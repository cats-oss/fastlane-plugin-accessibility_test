# coding: utf-8
require_relative '../helper/accessibility_evaluation_pb'
require_relative '../helper/github_notifier'
require_relative '../helper/helper'

module Fastlane
  module Actions
    class AccessibilityTestAction < Action
      def self.run(params)
        download_dir = params[:download_dir]
        firebase_test_lab_results_bucket = params[:firebase_test_lab_results_bucket] == nil ? "#{params[:project_id]}_test_results" : params[:firebase_test_lab_results_bucket]
        firebase_test_lab_results_dir = "firebase_test_result_#{DateTime.now.strftime('%Y-%m-%d-%H:%M:%S')}"
        devices = params[:devices]
        device_names = devices.map(&method(:device_name))
        results = []

        Fastlane::Actions::FirebaseTestLabAndroidAction.run(
            project_id: params[:project_id],
            gcloud_service_key_file: params[:gcloud_service_key_file],
            type: "robo",
            devices: devices,
            app_apk: params[:app_apk],
            console_log_file_name: "#{download_dir}/firebase_os_test_console.log",
            timeout: params[:timeout],
            notify_to_slack: false,
            extra_options: "--results-bucket #{firebase_test_lab_results_bucket} --results-dir #{firebase_test_lab_results_dir} --no-record-video"
        )

        UI.message "Fetch screenshots and accessibility meta data from Firebase Test Lab results bucket"
        device_names.each do |device_name|
          `mkdir -p #{download_dir}/#{device_name}`
          Action.sh "gsutil -m rsync -d -r gs://#{firebase_test_lab_results_bucket}/#{firebase_test_lab_results_dir}/#{device_name}/artifacts #{download_dir}/#{device_name}"
        end

        UI.message "Execute accessibility check"
        executable = File.expand_path('../../../../../bin/accessibility-analyzer.jar', __FILE__)
        device_names.each do |device_name|
          Action.sh "java -jar #{executable} --target=#{download_dir}/#{device_name}"
        end

        UI.message "Push screenshots and accessibility meta data from Firebase Test Lab results bucket"
        device_names.each do |device_name|
          `mkdir -p #{download_dir}/#{device_name}`
          Action.sh "gsutil -m rsync -d -r #{download_dir}/#{device_name} gs://#{firebase_test_lab_results_bucket}/#{firebase_test_lab_results_dir}/#{device_name}/artifacts"
        end

        UI.message "Extract test result"
        device_names.each do |device_name|
          entries = Dir::entries("#{download_dir}/#{device_name}")
          entries = entries.select { |entry| entry =~ /accessibility[0-9]+_check_result[0-9]+.meta/ }
          for entry in entries do
            filePath = "#{download_dir}/#{device_name}/#{entry}"
            File.open(filePath, "r") do |file|
              proto = Proto::AccessibilityHierarchyCheckResultProto.decode(file.read())
              results.push(
                {
                  title: proto.title,
                  message: proto.message,
                  image: Helper.firebase_object_url(firebase_test_lab_results_bucket, "#{firebase_test_lab_results_dir}/#{device_name}/artifacts/#{File.basename(filePath, ".meta")}.png")
                }
              )
            end
          end
        end

        UI.message "Notify to GitHub"
        notify_github(params, results)
      end

      def self.notify_github(params, results)
        return if params[:github_pr_number] == nil

        summary = results.empty? ? "### :white_check_mark: All test passed" : "### :x: #{results.length} error found."
        cells = results.map {|result|
          "|<img src=\"#{result[:image]}\">|**#{result[:title]}**<br/>#{result[:message]}|\n"
        }.inject(&:+)

        message = <<-EOS
## Accessibility Test Result
#{summary}

|Screenshot|message|
|-|-|
#{cells}
        EOS

        UI.message message
        GitHubNotifier.put_comment(
            params[:github_owner],
            params[:github_repository],
            params[:github_pr_number],
            message,
            params[:github_api_token]
        )
      end

      def self.device_name(device)
        "#{device[:model]}-#{device[:version]}-#{device[:locale]}-#{device[:orientation]}"
      end

      def self.description
        "Accessibility test with Firebase Test Lab for Android."
      end

      def self.details
        "Accessibility test with Firebase Test Lab for Android."
      end

      def self.authors
        ["Takeshi Tsukamoto"]
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(key: :project_id,
                                       env_name: "PROJECT_ID",
                                       description: "Your Firebase project id",
                                       type: String,
                                       optional: false),
          FastlaneCore::ConfigItem.new(key: :gcloud_service_key_file,
                                       env_name: "GCLOUD_SERVICE_KEY_FILE",
                                       description: "File path containing the gcloud auth key. Default: Created from GCLOUD_SERVICE_KEY environment variable",
                                       type: String,
                                       optional: false),
          FastlaneCore::ConfigItem.new(key: :devices,
                                       env_name: "DEVICES",
                                       description: "Devices to test the app on",
                                       type: Array,
                                       verify_block: proc do |value|
                                         UI.user_error!("Devices have to be at least one") if value.empty?
                                         value.each do |device|
                                           check_has_property(device, :model)
                                           check_has_property(device, :version)
                                           set_default_property(device, :locale, "en_US")
                                           set_default_property(device, :orientation, "portrait")
                                         end
                                       end),
          FastlaneCore::ConfigItem.new(key: :app_apk,
                                       env_name: "APP_APK",
                                       description: "The path for your android app apk",
                                       type: String,
                                       optional: false),
          FastlaneCore::ConfigItem.new(key: :timeout,
                                       env_name: "TIMEOUT",
                                       description: "The max time this test execution can run before it is cancelled. Default: 5m (this value must be greater than or equal to 1m)",
                                       type: String,
                                       optional: true,
                                       default_value: "5m"),
          FastlaneCore::ConfigItem.new(key: :firebase_test_lab_results_bucket,
                                       env_name: "FIREBASE_TEST_LAB_RESULTS_BUCKET",
                                       description: "Name of Firebase Test Lab results bucket",
                                       type: String,
                                       optional: true),
          FastlaneCore::ConfigItem.new(key: :download_dir,
                                       env_name: "DOWNLOAD_DIR",
                                       description: "Target directory to download screenshots from firebase",
                                       type: String,
                                       optional: false),
          FastlaneCore::ConfigItem.new(key: :github_owner,
                                       env_name: "GITHUB_OWNER",
                                       description: "Owner name",
                                       type: String,
                                       optional: false),
          FastlaneCore::ConfigItem.new(key: :github_repository,
                                       env_name: "GITHUB_REPOSITORY",
                                       description: "Repository name",
                                       type: String,
                                       optional: false),
          FastlaneCore::ConfigItem.new(key: :github_pr_number,
                                       env_name: "GITHUB_PR_NUMBER",
                                       description: "Pull request number",
                                       type: String,
                                       optional: true),
          FastlaneCore::ConfigItem.new(key: :github_api_token,
                                       env_name: "GITHUB_API_TOKEN",
                                       description: "GitHub API Token",
                                       type: String,
                                       optional: false),
        ]
      end

      def self.is_supported?(platform)
        platform == :android
      end

      def self.check_has_property(hash_obj, property)
        UI.user_error!("Each device must have #{property} property") unless hash_obj.key?(property)
      end

      def self.set_default_property(hash_obj, property, default)
        unless hash_obj.key?(property)
          hash_obj[property] = default
        end
      end
    end
  end
end

module Fastlane
  module Actions
    class AccessibilityTestAction < Action
      def self.run(params)
        download_dir = params[:download_dir]

        firebase_test_lab_results_bucket = params[:firebase_test_lab_results_bucket] == nil ? "#{params[:project_id]}_test_results" : params[:firebase_test_lab_results_bucket]
        firebase_test_lab_results_dir = "firebase_test_result_#{DateTime.now.strftime('%Y-%m-%d-%H:%M:%S')}"
        devices = params[:devices]
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
        device_names = devices.map(&method(:device_name))
        device_names.each do |device_name|
          `mkdir -p #{download_dir}/#{device_name}`
          Action.sh "gsutil -m rsync -d -r gs://#{firebase_test_lab_results_bucket}/#{firebase_test_lab_results_dir}/#{device_name}/artifacts #{download_dir}/#{device_name}"
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
                                       optional: false)
        ]
      end

      def self.is_supported?(platform)
        platform == :android
      end
    end
  end
end

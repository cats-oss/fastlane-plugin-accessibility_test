require 'fastlane_core/ui/ui'

module Fastlane
  module Helper
    def self.firebase_object_url(bucket, dir, path)
      "https://storage.cloud.google.com/#{bucket}/#{CGI.escape(dir)}/#{path}?authuser=1"
    end
  end
end

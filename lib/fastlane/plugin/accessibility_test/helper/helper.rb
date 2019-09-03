require 'fastlane_core/ui/ui'

module Fastlane
  module Helper
    def self.firebase_object_url(bucket, path)
      "https://firebasestorage.googleapis.com/v0/b/#{bucket}/o/#{CGI.escape(path)}?alt=media"
    end
  end
end

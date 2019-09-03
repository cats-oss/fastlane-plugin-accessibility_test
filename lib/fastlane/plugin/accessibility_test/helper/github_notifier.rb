require 'fastlane_core/ui/ui'
require 'json'
require 'net/http'
require 'uri'

module Fastlane
  module GitHubNotifier
    def self.put_comment(github_owner, github_repository, github_pr_number, body, github_api_token)
      api_url = "https://api.github.com/repos/#{github_owner}/#{github_repository}/issues/#{github_pr_number}/comments"
      UI.message "put comment #{api_url}"

      uri = URI.parse(api_url)
      req = Net::HTTP::Post.new(uri)
      req["Content-Type"] = "application/json"
      req["Authorization"] = "token #{github_api_token}"
      req.body = {:body => body}.to_json

      res = Net::HTTP.start(uri.hostname, uri.port, {use_ssl: uri.scheme = "https"}) {|http| http.request(req)}
      UI.message "#{res.code}\n#{res.body}"

      res
    end
  end
end

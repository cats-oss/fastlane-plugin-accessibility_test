# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: AccessibilityEvaluation.proto

require 'google/protobuf'

Google::Protobuf::DescriptorPool.generated_pool.build do
  add_file("AccessibilityEvaluation.proto", :syntax => :proto2) do
    add_message "proto.AccessibilityHierarchyCheckResultProto" do
      optional :source_check_class, :string, 1
      optional :result_id, :int32, 2
      optional :result_type, :enum, 3, "proto.ResultTypeProto"
      optional :hierarchy_source_id, :int64, 4
      optional :title, :string, 6
      optional :message, :string, 7
    end
    add_enum "proto.ResultTypeProto" do
      value :UNKNOWN, 0
      value :ERROR, 1
      value :WARNING, 2
      value :INFO, 3
      value :NOT_RUN, 4
      value :SUPPRESSED, 5
    end
  end
end

module Proto
  AccessibilityHierarchyCheckResultProto = Google::Protobuf::DescriptorPool.generated_pool.lookup("proto.AccessibilityHierarchyCheckResultProto").msgclass
  ResultTypeProto = Google::Protobuf::DescriptorPool.generated_pool.lookup("proto.ResultTypeProto").enummodule
end

describe Fastlane::Actions::AccessibilityTestAction do
  describe '#run' do
    it 'prints a message' do
      expect(Fastlane::UI).to receive(:message).with("The accessibility_test plugin is working!")

      Fastlane::Actions::AccessibilityTestAction.run(nil)
    end
  end
end

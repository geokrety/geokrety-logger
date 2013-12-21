package pl.nkg.lib.dialogs;

public class GenericProgressDialogWrapper extends
		AbstractProgressDialogWrapper<String> {

	public GenericProgressDialogWrapper(ManagedDialogsActivity a, int dialogId) {
		super(a, dialogId);
	}

	@Override
	public void updateProgress() {
		setMessage(getProgres());
	}
}

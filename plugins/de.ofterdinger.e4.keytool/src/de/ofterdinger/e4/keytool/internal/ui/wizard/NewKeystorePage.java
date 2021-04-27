package de.ofterdinger.e4.keytool.internal.ui.wizard;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.ofterdinger.e4.keytool.internal.certificate.KeystoreType;
import de.ofterdinger.e4.keytool.internal.ui.util.KeystoreUIHelper;

public class NewKeystorePage extends AbstractWizardPage {
	private static final int NO_OF_COLUMNS = 3;
	private Text filenameText;
	private Combo keystoreTypeCombo;
	private Text passwordText;

	public NewKeystorePage() {
		this("Select keystore to create"); //$NON-NLS-1$
	}

	public NewKeystorePage(String pageName) {
		super(pageName);
		setPageComplete(false);
		setTitle("Create a new certificate"); //$NON-NLS-1$
		setDescription(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, 0);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = NO_OF_COLUMNS;
		addFilenameInputs(container);
		this.passwordText = this.makeLine(container, "Keystore password", EMPTY); //$NON-NLS-1$
		new org.eclipse.swt.widgets.Label(container, 0);
		new Label(container, 0).setText("Keystore type"); //$NON-NLS-1$
		this.keystoreTypeCombo = new Combo(container, 12);
		KeystoreUIHelper.populateAvailableKeystoreTypes(this.keystoreTypeCombo);
		setControl(container);
	}

	public String getFilename() {
		return this.filenameText.getText();
	}

	public KeystoreType getKeystoreType() {
		return KeystoreType.getInstance(this.keystoreTypeCombo.getText());
	}

	@Override
	public IWizardPage getNextPage() {
		IWizard theWizard = getWizard();
		if (theWizard instanceof NewCertificateWizard) {
			ShowCertificatePage showCertificatePage = ((NewCertificateWizard) theWizard).getShowCertificatePage();
			showCertificatePage.setKeystore(this.filenameText.getText());
			return showCertificatePage;
		}
		return null;
	}

	public String getPassword() {
		return this.passwordText.getText();
	}

	void handleBrowse() {
		FileDialog dialog = new FileDialog(getShell(), 4096);
		dialog.setText("Create keystore"); //$NON-NLS-1$
		String file = dialog.open();
		dialogChanged();
		if (StringUtils.isNotBlank(file)) {
			this.filenameText.setText(file);
		}
	}

	@Override
	protected void dialogChanged() {
		if (this.filenameText != null) {
			this.checkStatus(this.filenameText, "Filename must be specified."); //$NON-NLS-1$
			if (this.filenameText.getText().length() > 0) {
				updateStatus(null);
			}
			this.checkStatus(this.passwordText, "Password for the keystore must be specified."); //$NON-NLS-1$
		}
	}

	private void addFilenameInputs(Composite container) {
		Label label = new Label(container, 0);
		label.setText("Filename"); //$NON-NLS-1$
		GridData gridData = new GridData(4, 2, true, false);
		this.filenameText = new Text(container, 2048);
		this.filenameText.setLayoutData(gridData);
		this.filenameText.setText(EMPTY);
		this.filenameText.setEditable(true);
		this.filenameText.addModifyListener(getModifyListener());
		Button button = new Button(container, 8);
		button.setText("Browse..."); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewKeystorePage.this.handleBrowse();
			}
		});
	}
}

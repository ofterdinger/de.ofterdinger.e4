package de.ofterdinger.e4.keytool.internal.ui.wizard;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.eclipse.swt.SWT.CENTER;
import static org.eclipse.swt.SWT.FILL;
import static org.eclipse.swt.SWT.LEFT;

import java.io.File;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ImportCertificatePage extends AbstractShowCertificatePage {
	Button importPrivateKeyCheckBox;
	Text password;
	private Text alias;
	private Text filenameText;
	private File fileToImport = null;
	private Combo keystoreCombo;

	protected ImportCertificatePage(String pageName) {
		super(pageName);
		setDescription("Import a certificate. Either with or without a private key."); //$NON-NLS-1$
		setPageComplete(false);
		setTitle("Import a certificate"); //$NON-NLS-1$
	}

	@Override
	public boolean canFlipToNextPage() {
		return (isNewKeystore() && fieldsArePresent());
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, 0);
		GridLayout layout = new GridLayout(3, false);
		composite.setLayout(layout);
		this.keystoreCombo = addOpenKeystores(composite, null);
		this.keystoreCombo.setLayoutData(new GridData(FILL, CENTER, true, false, 2, 1));

		Label filenameLabel = new Label(composite, 0);
		filenameLabel.setText("&Filename:"); //$NON-NLS-1$
		filenameLabel.setLayoutData(new GridData(FILL, CENTER, false, false));

		this.filenameText = new Text(composite, 2048);
		this.filenameText.setLayoutData(new GridData(FILL, CENTER, true, false));
		this.filenameText.addModifyListener(getModifyListener());
		addBrowseButton(composite, this.filenameText);
		if (this.fileToImport != null) {
			this.filenameText.setText(this.fileToImport.getAbsolutePath());
		}

		this.alias = this.makeLine(composite, "&Alias", EMPTY); //$NON-NLS-1$
		this.alias.setLayoutData(new GridData(FILL, CENTER, true, false, 2, 1));

		this.importPrivateKeyCheckBox = new Button(composite, 32);
		this.importPrivateKeyCheckBox.setText("Contains private key"); //$NON-NLS-1$
		this.importPrivateKeyCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (ImportCertificatePage.this.importPrivateKeyCheckBox.getSelection()) {
					ImportCertificatePage.this.password.setEditable(true);
					ImportCertificatePage.this.password.setEnabled(true);
				} else {
					ImportCertificatePage.this.password.setEditable(false);
					ImportCertificatePage.this.password.setEnabled(false);
				}
				ImportCertificatePage.this.dialogChanged();
			}
		});
		this.importPrivateKeyCheckBox.setLayoutData(new GridData(FILL, LEFT, true, false, 3, 1));
		this.password = this.makeLine(composite, "&Certificate password", EMPTY, 4196352); //$NON-NLS-1$
		this.password.setEditable(false);
		this.password.setEnabled(false);
		setControl(composite);
		updateStatus(null);
	}

	public String getAliasText() {
		return this.alias.getText();
	}

	public String getFilename() {
		return this.filenameText.getText();
	}

	public String getKeystoreFilename() {
		return this.keystoreCombo.getText();
	}

	@Override
	public IWizardPage getNextPage() {
		return getNewKeystorePage();
	}

	public String getPassword() {
		return this.password.getText();
	}

	public boolean isNewKeystore() {
		return AbstractWizardPage.CREATE_A_NEW_KEYSTORE_TEXT.equals(this.keystoreCombo == null ? null : this.keystoreCombo.getText());
	}

	@Override
	public boolean isPageComplete() {
		return fieldsArePresent();
	}

	public boolean isWithPrivateKey() {
		if (this.importPrivateKeyCheckBox == null)
			return false;
		return this.importPrivateKeyCheckBox.getSelection();
	}

	public void setFileToImport(File file) {
		this.fileToImport = file;
	}

	@Override
	protected void dialogChanged() {
		updateStatus(null);
		this.checkStatus(this.filenameText, "Filename must be specified"); //$NON-NLS-1$
		if (this.importPrivateKeyCheckBox != null && this.importPrivateKeyCheckBox.getSelection()) {
			this.checkStatus(this.password, "Password must be specified"); //$NON-NLS-1$
		}
		this.checkStatus(this.alias, "Alias must be specified"); //$NON-NLS-1$
	}

	private boolean fieldsArePresent() {
		return (!(getFilename().length() <= 0 || isWithPrivateKey() && getPassword().length() <= 0));
	}

	private NewKeystorePage getNewKeystorePage() {
		return ((ImportCertificateWizard) getWizard()).getNewKeystorePage();
	}

}

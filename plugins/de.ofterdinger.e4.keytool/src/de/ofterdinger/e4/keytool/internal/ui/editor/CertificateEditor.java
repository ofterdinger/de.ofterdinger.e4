package de.ofterdinger.e4.keytool.internal.ui.editor;

import static de.ofterdinger.e4.keytool.internal.KeytoolPlugin.PLUGIN_ID;
import static de.ofterdinger.e4.keytool.internal.certificate.Algorithm.MD5;
import static de.ofterdinger.e4.keytool.internal.certificate.Algorithm.SHA1;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.eclipse.core.runtime.IStatus.ERROR;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;

import de.ofterdinger.e4.keytool.internal.KeystoreAdapterFactory;
import de.ofterdinger.e4.keytool.internal.KeytoolPlugin;
import de.ofterdinger.e4.keytool.internal.certificate.CertTools;
import de.ofterdinger.e4.keytool.internal.certificate.CompleteCertificate;
import de.ofterdinger.e4.keytool.internal.ui.KeystoreViewLabelProvider;
import de.ofterdinger.e4.keytool.internal.ui.dialog.PasswordDialog;
import de.ofterdinger.e4.keytool.internal.ui.util.ImageKeys;
import de.ofterdinger.e4.keytool.internal.ui.util.TextConstants;
import de.ofterdinger.e4.keytool.internal.ui.util.TreeChainObject;

public class CertificateEditor extends MultiPageEditorPart implements IResourceChangeListener {
	private static final String COULD_NOT_GENERATE_CSR = "*** Could not generate CSR - Possible wrong certificate password ***"; //$NON-NLS-1$
	private static final int FONT_HEIGHT = 23;
	private static final String FOR_CERTIFICATE_NOT_SETTED = "Password for certificate not setted"; //$NON-NLS-1$
	private static HashMap<String, Color> resources = new HashMap<>();
	private static final String SETTING_CSR = "Setting CSR..."; //$NON-NLS-1$
	Text csrText;
	private final IAdapterFactory adapterFactory = new KeystoreAdapterFactory();
	private CompleteCertificate completeCertificate;
	private ExportCertificateAction exportAction;

	public CertificateEditor() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		Platform.getAdapterManager().unregisterAdapters(this.adapterFactory);
		super.dispose();
	}

	@Override
	public final void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}

	public final CompleteCertificate getCompleteCertificate() {
		return this.completeCertificate;
	}

	@Override
	public final void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (editorInput instanceof IFileEditorInput) {
			loadCertificate(editorInput);
		} else if (editorInput instanceof CertificateInput) {
			CertificateInput ci = (CertificateInput) editorInput;
			this.completeCertificate = ci.getCompleteCertificate();
			setPartName(ci.getName());
			setTitleToolTip(ci.getName());
			getConfigurationElement();
		} else {
			throw new PartInitException("Invalid Input: Must be IFileEditorInput or CertificateInput"); //$NON-NLS-1$
		}
		super.init(site, editorInput);
	}

	@Override
	public final boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public final void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == 2) {
			Display.getDefault().asyncExec(() -> {
			});
		}
	}

	@Override
	protected final void createPages() {
		Platform.getAdapterManager().registerAdapters(this.adapterFactory, TreeChainObject.class);
		if (getPartName().length() == 0 || getPartName().equals("Certificate editor")) { //$NON-NLS-1$
			String partName = getTitleToolTip();
			if (getTitleToolTip().indexOf('/') > -1) {
				partName = getTitleToolTip().substring(getTitleToolTip().lastIndexOf('/') + 1);
			}
			setPartName(partName);
		}
		createCertificatePage(this.completeCertificate);
		createCertificateChainPage(this.completeCertificate);
		if (this.completeCertificate.isKeyEntry()) {
			createCSRPage(this.completeCertificate);
		}
		hookContextMenu(getSite().getShell(), getContainer());
	}

	@Override
	protected final void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
	}

	private void createCertificateChainPage(CompleteCertificate compCertificate) {
		try {
			Certificate[] certificateChain = CertTools.getCertificateChain(compCertificate);
			if (certificateChain == null || certificateChain.length == 1) {
				return;
			}
			Composite composite = new Composite(getContainer(), 0);
			Color backgroundColor = getContainer().getDisplay().getSystemColor(25);
			composite.setBackground(backgroundColor);
			composite.setLayout(new FillLayout());
			TreeViewer viewer = new TreeViewer(composite, 3842);
			viewer.setContentProvider(new ViewContentProvider());
			viewer.setInput(CertificateEditor.createModel(certificateChain, compCertificate));
			viewer.setLabelProvider(new KeystoreViewLabelProvider());
			viewer.expandAll();
			int index = this.addPage(composite);
			setPageText(index, "Certificatechain"); //$NON-NLS-1$
			setPageImage(index, ImageKeys.getImage(ImageKeys.CERTIFICATE));
			PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, TextConstants.CERTIFICATE_EDITOR_HELP_ID);
		} catch (KeyStoreException e) {
			KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
		}
	}

	private void createCertificatePage(CompleteCertificate compCertificate) {
		Certificate certificate = compCertificate.getCertificate();
		Composite composite = new Composite(getContainer(), 0);
		Color backgroundColor = getContainer().getDisplay().getSystemColor(25);
		composite.setBackground(backgroundColor);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		Label titleLabel = new Label(composite, 0);
		titleLabel.setText("Certificate"); //$NON-NLS-1$
		titleLabel.setBackground(backgroundColor);
		titleLabel.setForeground(getContainer().getDisplay().getSystemColor(9));
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		titleLabel.setLayoutData(gd);
		FontData fontData = new FontData("Times New Roman", FONT_HEIGHT, 0); //$NON-NLS-1$
		Font font2 = new Font(titleLabel.getDisplay(), fontData);
		titleLabel.setFont(font2);
		try {
			if (certificate instanceof X509Certificate) {
				X509Certificate x509 = (X509Certificate) certificate;
				makeLine(composite, "Owner", x509.getSubjectX500Principal().getName()); //$NON-NLS-1$
				makeLine(composite, "Issuer", x509.getIssuerX500Principal().getName()); //$NON-NLS-1$
				makeLine(composite, "Valid from", x509.getNotBefore().toString()); //$NON-NLS-1$
				makeLine(composite, "Valid to", x509.getNotAfter().toString()); //$NON-NLS-1$
			}
			makeLine(composite, "Serial Number", CertTools.getSerialNumber(certificate)); //$NON-NLS-1$
			makeLine(composite, "MD5 Fingerprint", CertTools.generateFingerprintInHex(certificate, MD5)); //$NON-NLS-1$
			makeLine(composite, "SHA1 Fingerprint", CertTools.generateFingerprintInHex(certificate, SHA1)); //$NON-NLS-1$
		} catch (Exception e) {
			KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
		}
		hookContextMenu(getSite().getShell(), composite);
		int index = this.addPage(composite);
		setPageText(index, "Certificate information"); //$NON-NLS-1$
		if (compCertificate.isKeyEntry()) {
			setPageImage(index, ImageKeys.getImage(ImageKeys.LOCK_CLOSED));
		} else {
			setPageImage(index, ImageKeys.getImage(ImageKeys.LOCK_OPEN));
		}
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, TextConstants.CERTIFICATE_EDITOR_HELP_ID);
	}

	private void createCSRPage(CompleteCertificate compCertificate) {
		Composite composite = new Composite(getContainer(), 0);
		Color backgroundColor = getContainer().getDisplay().getSystemColor(25);
		composite.setBackground(backgroundColor);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);
		Label titleLabel = new Label(composite, 0);
		titleLabel.setText("Certificate Signing Request (CSR)"); //$NON-NLS-1$
		titleLabel.setBackground(backgroundColor);
		titleLabel.setForeground(getContainer().getDisplay().getSystemColor(9));
		this.csrText = new Text(composite, 2818);
		this.csrText.setLayoutData(new GridData(1808));
		this.csrText.setText(SETTING_CSR);
		this.csrText.setEditable(false);
		int index = this.addPage(composite);
		setPageText(index, "CSR"); //$NON-NLS-1$
		setPageImage(index, ImageKeys.getImage(ImageKeys.CERTIFICATE));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, TextConstants.CERTIFICATE_EDITOR_HELP_ID);
		Listener listener = new Listener() {
			boolean isCallingCSR = false;

			@Override
			public void handleEvent(Event arg0) {
				if (!this.isCallingCSR) {
					this.isCallingCSR = true;
					CertificateEditor.setCSRText(CertificateEditor.this.getSite().getShell(), compCertificate,
							CertificateEditor.this.csrText);
					this.isCallingCSR = false;
				}
			}
		};
		composite.addListener(26, listener);
	}

	private void hookContextMenu(Shell shell, Control control) {
		if (this.exportAction == null) {
			this.exportAction = new ExportCertificateAction(this.completeCertificate);
			this.exportAction.setShell(shell);
		}
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(manager -> {
			manager.add(CertificateEditor.this.exportAction);
			manager.add(new Separator("additions")); //$NON-NLS-1$
		});
		Menu menu = menuMgr.createContextMenu(control);
		control.setMenu(menu);
	}

	private void loadCertificate(IEditorInput editorInput) {
		IFileEditorInput ifei = (IFileEditorInput) editorInput;
		this.completeCertificate = new CompleteCertificate();
		try (InputStream contents = ifei.getFile().getContents()) {
			this.completeCertificate.setCertificate(CertTools.loadCertificate(contents));
		} catch (IOException | CoreException | CertificateException e) {
			KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
		}
	}

	private Text makeLine(Composite composite, String labelText, String value) {
		Color backgroundColor = getContainer().getDisplay().getSystemColor(25);
		Label label = new Label(composite, 0);
		label.setText(labelText);
		label.setBackground(backgroundColor);
		GridData gridData = new GridData(4, 4, true, false);
		Text text = new Text(composite, 2048);
		text.setLayoutData(gridData);
		text.setText(value);
		text.setEditable(false);
		return text;
	}

	public static Color getColor(int red, int green, int blue) {
		String name = "COLOR:" + red + "," + green + "," + blue; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (resources.containsKey(name)) {
			return resources.get(name);
		}
		Color color = new Color(Display.getDefault(), red, green, blue);
		resources.put(name, color);
		return color;
	}

	static void setCSRText(Shell parentShell, CompleteCertificate completeCertificate, Text text) {
		block8: {
			String passInput = text.getText();
			if (SETTING_CSR.equals(passInput) || FOR_CERTIFICATE_NOT_SETTED.equals(passInput) || COULD_NOT_GENERATE_CSR.equals(passInput)) {
				PasswordDialog pd = new PasswordDialog(parentShell, "Enter certificate password", "Enter certificate password", EMPTY, //$NON-NLS-1$ //$NON-NLS-2$
						null);
				int result = pd.open();
				if (result == 0) {
					completeCertificate.setPassword(pd.getValue());
				} else {
					completeCertificate.setPassword(null);
				}
				if (StringUtils.isNotEmpty(completeCertificate.getPassword())) {
					try {
						if (completeCertificate.getPrivateKey() != null) {
							text.setText(CertTools.generateCSR(completeCertificate));
							break block8;
						}
						CompleteCertificate certWithPrivate = CertTools.getCompleteCertificate(completeCertificate.getKeystoreFile(),
								completeCertificate.getAlias(), completeCertificate.getPassword());
						text.setText(CertTools.generateCSR(certWithPrivate));
					} catch (Exception e) {
						text.setText(COULD_NOT_GENERATE_CSR);
						KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
					}
				} else {
					text.setText(FOR_CERTIFICATE_NOT_SETTED);
				}
			}
		}
	}

	protected static Object createModel(Certificate[] certificateChain, CompleteCertificate completeCertificate) {
		TreeChainObject root;
		TreeChainObject nextRoot = root = new TreeChainObject(EMPTY);
		X509Certificate x509 = (X509Certificate) completeCertificate.getCertificate();
		String displayName = x509.getSubjectX500Principal().getName();
		if (certificateChain != null) {
			int i = 0;
			while (i < certificateChain.length) {
				Certificate certificate = certificateChain[i];
				String displayNameChain = certificate.toString();
				if (certificate instanceof X509Certificate) {
					x509 = (X509Certificate) certificate;
					displayNameChain = x509.getIssuerX500Principal().getName();
				}
				if (!displayName.equals(displayNameChain)) {
					TreeChainObject nextChild = new TreeChainObject(displayNameChain);
					nextChild.setParent(nextRoot);
					nextRoot.setChild(nextChild);
					nextRoot = nextChild;
				}
				++i;
			}
		}
		return root;
	}

	public class ViewContentProvider implements ITreeContentProvider {
		@Override
		public void dispose() {
			// nothing to do yet
		}

		@Override
		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeChainObject) {
				TreeChainObject tco = (TreeChainObject) parent;
				if (tco.hasChild()) {
					return new Object[] { tco.getChild() };
				}
				return new Object[] { parent };
			}
			return new Object[0];
		}

		@Override
		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}

		@Override
		public Object getParent(Object child) {
			if (child instanceof TreeChainObject) {
				TreeChainObject tco = ((TreeChainObject) child);
				return tco.hasParent() ? tco.getParent() : null;
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeChainObject) {
				TreeChainObject tco = (TreeChainObject) parent;
				return tco.hasChild();
			}
			return false;
		}

		@Override
		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
			// nothing to do yet
		}
	}
}

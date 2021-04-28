package de.ofterdinger.e4.keytool.internal.ui;

import static de.ofterdinger.e4.keytool.internal.KeytoolPlugin.PLUGIN_ID;
import static de.ofterdinger.e4.keytool.internal.filechanged.FileChangedEvent.FILE_ADDED;
import static de.ofterdinger.e4.keytool.internal.filechanged.FileChangedEvent.FILE_UPDATED;
import static de.ofterdinger.e4.keytool.internal.ui.util.ImageKeys.NEW_CERTIFICATE;
import static de.ofterdinger.e4.keytool.internal.ui.util.TextConstants.CERTIFICATE_EDITOR_ID;
import static de.ofterdinger.e4.keytool.internal.ui.util.TextConstants.KEYSTORE_VIEW_HELP_ID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.eclipse.core.runtime.IStatus.ERROR;

import de.ofterdinger.e4.keytool.internal.KeystoreAdapterFactory;
import de.ofterdinger.e4.keytool.internal.KeystoreFile;
import de.ofterdinger.e4.keytool.internal.KeytoolPlugin;
import de.ofterdinger.e4.keytool.internal.certificate.CertTools;
import de.ofterdinger.e4.keytool.internal.certificate.CompleteCertificate;
import de.ofterdinger.e4.keytool.internal.certificate.KeystoreType;
import de.ofterdinger.e4.keytool.internal.filechanged.FileChangeMonitor;
import de.ofterdinger.e4.keytool.internal.filechanged.FileChangedEvent;
import de.ofterdinger.e4.keytool.internal.filechanged.IFileChangeListener;
import de.ofterdinger.e4.keytool.internal.ui.action.CloseKeystoreAction;
import de.ofterdinger.e4.keytool.internal.ui.action.DeleteCertificateFromViewAction;
import de.ofterdinger.e4.keytool.internal.ui.action.ExportCertificateFromViewAction;
import de.ofterdinger.e4.keytool.internal.ui.dialog.OpenKeyStoreDialog;
import de.ofterdinger.e4.keytool.internal.ui.editor.CertificateInput;
import de.ofterdinger.e4.keytool.internal.ui.editor.ImportCertificateAction;
import de.ofterdinger.e4.keytool.internal.ui.preference.GeneralPreferencePage;
import de.ofterdinger.e4.keytool.internal.ui.preference.OpenPreferencesAction;
import de.ofterdinger.e4.keytool.internal.ui.util.ImageKeys;
import de.ofterdinger.e4.keytool.internal.ui.util.TreeObject;
import de.ofterdinger.e4.keytool.internal.ui.util.TreeParent;
import de.ofterdinger.e4.keytool.internal.ui.util.TreeUpdater;
import de.ofterdinger.e4.keytool.internal.ui.wizard.NewCertificateWizard;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class KeyStoreView extends AbstractNavigationView implements IFileChangeListener {
  private static KeyStoreView instance;
  private static List<KeystoreFile> keystores = new ArrayList<>();
  private static FileChangeMonitor monitor;
  CloseKeystoreAction closeKeystoreAction;
  DeleteCertificateFromViewAction deleteCertificateAction;
  ExportCertificateFromViewAction exportCertificateAction;
  private final IAdapterFactory adapterFactory = new KeystoreAdapterFactory();
  private Action doubleClickAction;
  private ImportCertificateAction importCertificateAction;
  private Action newCertificateAction;
  private Action openKeystoreAction;
  private OpenPreferencesAction openPreferencesAction;
  private final IPropertyChangeListener preferenceListener;

  public KeyStoreView() {
    this.preferenceListener =
        event -> {
          monitor.stopMonitor();
          monitor.setScanRate(GeneralPreferencePage.getFilechangeMonitorInterval());
          monitor.startMonitor();
        };
    setInstance(this);
  }

  @Override
  public final void createPartControl(Composite parent) {
    Platform.getAdapterManager().registerAdapters(this.adapterFactory, TreeObject.class);
    super.createTreeViewer(parent);
    PlatformUI.getWorkbench()
        .getHelpSystem()
        .setHelp(getViewer().getControl(), KEYSTORE_VIEW_HELP_ID);
    loadAutostartupFile();
    makeActions(getSite().getWorkbenchWindow());
    hookContextMenu();
    hookDoubleClickAction();
    contributeToActionBars();
    getViewer().setLabelProvider(new KeystoreViewLabelProvider());
    getViewSite()
        .getPage()
        .addPartListener(
            new IPartListener() {
              @Override
              public void partActivated(IWorkbenchPart part) {
                ISelection selection = KeyStoreView.this.getViewer().getSelection();
                KeyStoreView.this.closeKeystoreAction.checkSelection(selection);
                KeyStoreView.this.exportCertificateAction.checkSelection(selection);
                KeyStoreView.this.deleteCertificateAction.checkSelection(selection);
              }

              @Override
              public void partBroughtToTop(IWorkbenchPart part) {
                // nothing to do yet
              }

              @Override
              public void partClosed(IWorkbenchPart part) {
                // nothing to do yet
              }

              @Override
              public void partDeactivated(IWorkbenchPart part) {
                // nothing to do yet
              }

              @Override
              public void partOpened(IWorkbenchPart part) {
                // nothing to do yet
              }
            });
  }

  @Override
  public final void dispose() {
    getViewer().removeSelectionChangedListener(this.closeKeystoreAction);
    getViewer().removeSelectionChangedListener(this.exportCertificateAction);
    getViewer().removeSelectionChangedListener(this.deleteCertificateAction);
    Platform.getAdapterManager().unregisterAdapters(this.adapterFactory);
    KeytoolPlugin.getDefault()
        .getPreferenceStore()
        .removePropertyChangeListener(this.preferenceListener);
    super.dispose();
  }

  @Override
  public final void fileChanged(FileChangedEvent[] evt) {
    int i = 0;
    while (i < evt.length) {
      updateTree(evt[i]);
      ++i;
    }
  }

  @Override
  public final void init(IViewSite site) throws PartInitException {
    super.init(site);
    KeytoolPlugin.getDefault()
        .getPreferenceStore()
        .addPropertyChangeListener(this.preferenceListener);
  }

  public void openKeystore() {
    openKeyStoreInstance();
  }

  void openKeyStoreInstance() {
    OpenKeyStoreDialog openDialog = new OpenKeyStoreDialog(null);
    if (openDialog.open() != 0) {
      return;
    }
    String filename = openDialog.getFilename();
    KeystoreType keystoreType = openDialog.getKeystoreType();
    String password = openDialog.getPassword();
    loadKeystoreFile(filename, keystoreType, password);
  }

  @Override
  protected final Object createModel() {
    try {
      TreeParent p1 = new TreeParent(null, EMPTY);
      if (keystores != null && !keystores.isEmpty()) {
        int i = 0;
        while (i < keystores.size()) {
          addKeystoreNode(p1, keystores.get(i));
          ++i;
        }
      }
      return p1;
    } catch (Exception e) {
      KeytoolPlugin.showError("Error creating model for tree!", getSite().getShell());
      KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
      return null;
    }
  }

  private void contributeToActionBars() {
    IActionBars bars = getViewSite().getActionBars();
    fillLocalPullDown(bars.getMenuManager());
    fillLocalToolBar(bars.getToolBarManager());
  }

  private void fillContextMenu(IMenuManager manager) {
    manager.add(this.newCertificateAction);
    manager.add(new Separator());
    manager.add(this.closeKeystoreAction);
    manager.add(this.exportCertificateAction);
    manager.add(this.deleteCertificateAction);
    manager.add(this.importCertificateAction);
    manager.add(new Separator());
    manager.add(this.openKeystoreAction);
    manager.add(new Separator("additions"));
  }

  private void fillLocalPullDown(IMenuManager manager) {
    manager.add(this.openKeystoreAction);
    manager.add(this.closeKeystoreAction);
    manager.add(new Separator());
    manager.add(this.openPreferencesAction);
  }

  private void fillLocalToolBar(IToolBarManager manager) {
    manager.add(this.openKeystoreAction);
  }

  private void hookContextMenu() {
    MenuManager menuMgr = new MenuManager("#PopupMenu");
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(KeyStoreView.this::fillContextMenu);
    Menu menu = menuMgr.createContextMenu(getViewer().getControl());
    getViewer().getControl().setMenu(menu);
    getSite().registerContextMenu(menuMgr, getViewer());
  }

  private void hookDoubleClickAction() {
    getViewer()
        .addDoubleClickListener(
            event -> {
              if (event.getSource() instanceof TreeViewer) {
                KeyStoreView.this.doubleClickAction.run();
              }
            });
  }

  private void loadAutostartupFile() {
    String keystoreFile = GeneralPreferencePage.getKeystoreFile();
    if (keystoreFile == null || keystoreFile.length() == 0) {
      return;
    }
    if (keystores != null) {
      Iterator<KeystoreFile> iterator = keystores.iterator();
      while (iterator.hasNext()) {
        if (!iterator.next().getKeystorefilename().equals(keystoreFile)) {
          continue;
        }
        return;
      }
    }
    KeystoreType keystoreType = GeneralPreferencePage.getKeystoreType();
    String keystorePassword = GeneralPreferencePage.getKeystorePassword();
    if (keystoreFile.length() > 0) {
      loadKeystoreFile(keystoreFile, keystoreType, keystorePassword);
    }
  }

  private void loadKeystoreFile(
      String keystoreFilename, KeystoreType keystoreType, String keystorePassword) {
    monitor.stopMonitor();
    try {
      KeystoreFile keystoreFile =
          CertTools.loadKeystoreFile(keystoreFilename, keystoreType, keystorePassword);
      keystores.add(keystoreFile);
      getViewer().setAutoExpandLevel(-1);
      TreeParent parent = (TreeParent) getViewer().getInput();
      addKeystoreNode(parent, keystoreFile);
      getViewer().refresh();
      monitor.addSource(new File(keystoreFilename));
    } catch (Exception e) {
      KeytoolPlugin.showError("Error loading keystore!", getSite().getShell());
      KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
    }
    monitor.startMonitor();
  }

  private void makeActions(IWorkbenchWindow window) {
    this.openKeystoreAction =
        new Action() {

          @Override
          public void run() {
            KeyStoreView.this.openKeyStoreInstance();
          }
        };
    this.openKeystoreAction.setText("Open Keystore");
    this.openKeystoreAction.setToolTipText("Open a keystore");
    this.openKeystoreAction.setImageDescriptor(
        PlatformUI.getWorkbench().getSharedImages().getImageDescriptor("IMG_OBJ_FOLDER"));
    this.closeKeystoreAction = new CloseKeystoreAction(window, getViewer());
    getViewer().addSelectionChangedListener(this.closeKeystoreAction);
    this.exportCertificateAction = new ExportCertificateFromViewAction(window);
    getViewer().addSelectionChangedListener(this.exportCertificateAction);
    this.deleteCertificateAction = new DeleteCertificateFromViewAction(window);
    getViewer().addSelectionChangedListener(this.deleteCertificateAction);
    this.importCertificateAction = new ImportCertificateAction(window);
    this.openPreferencesAction = new OpenPreferencesAction(window);
    this.openPreferencesAction.setText("Preferences...");
    this.newCertificateAction =
        new Action("Create new certificate") {

          @Override
          public void run() {
            super.run();
            ISelection selection = KeyStoreView.this.getViewer().getSelection();
            TreeObject treeObj = (TreeObject) ((IStructuredSelection) selection).getFirstElement();
            TreeParent treeParent = null;
            if (treeObj != null) {
              treeParent =
                  treeObj instanceof TreeParent ? (TreeParent) treeObj : treeObj.getParent();
            }
            NewCertificateWizard wizard = new NewCertificateWizard(treeParent);
            wizard.setForcePreviousAndNextButtons(true);
            WizardDialog dialog = new WizardDialog(KeyStoreView.this.getSite().getShell(), wizard);
            dialog.create();
            dialog.open();
          }
        };
    this.newCertificateAction.setImageDescriptor(ImageKeys.getImageDescriptor(NEW_CERTIFICATE));
    this.doubleClickAction =
        new Action() {

          @Override
          public void run() {
            ISelection selection = KeyStoreView.this.getViewer().getSelection();
            TreeObject treeObj = (TreeObject) ((IStructuredSelection) selection).getFirstElement();
            if (treeObj.hasParentKeystore()) {
              try {
                CompleteCertificate certificate =
                    CertTools.getCertificate(
                        treeObj.getParent().getKeystoreFile(), treeObj.getName());
                CertificateInput ci = new CertificateInput(certificate);
                KeyStoreView.this.getSite().getPage().openEditor(ci, CERTIFICATE_EDITOR_ID);
              } catch (Exception e) {
                KeytoolPlugin.showError(
                    "Error showing certificate!", KeyStoreView.this.getSite().getShell());
                KeytoolPlugin.getDefault()
                    .getLog()
                    .log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
              }
            }
          }
        };
  }

  private void updateTree(FileChangedEvent aEvt) {
    Display display = getSite().getWorkbenchWindow().getWorkbench().getDisplay();
    display.syncExec(new TreeUpdater(aEvt, getViewer()));
  }

  public static void addMonitorFile(
      String keystoreFilename, KeystoreType keystoreType, String keystorePassword) {
    if (monitor != null) {
      monitor.postFileChangedEvent(
          new FileChangedEvent(FILE_ADDED, keystoreFilename, keystorePassword, keystoreType));
      monitor.stopMonitor();
      monitor.startMonitor();
    }
  }

  public static void fireFileChanged(
      String keystoreFilename, KeystoreType keystoreType, String keystorePassword) {
    if (monitor != null) {
      monitor.postFileChangedEvent(
          new FileChangedEvent(FILE_UPDATED, keystoreFilename, keystorePassword, keystoreType));
    }
  }

  public static KeyStoreView getInstance() {
    return instance;
  }

  public static KeystoreFile getKeystoreFile(String filename) {
    int i = 0;
    while (i < keystores.size()) {
      KeystoreFile file = keystores.get(i);
      if (file.getKeystorefilename().equals(filename)) {
        return file;
      }
      ++i;
    }
    return null;
  }

  public static List<KeystoreFile> getKeystores() {
    return keystores;
  }

  private static void addKeystoreNode(TreeParent parentNode, KeystoreFile keystoreFile) {
    TreeParent keystoreNode = new TreeParent(keystoreFile, keystoreFile.getKeystorefilename());
    keystoreNode.populateKeystoreToNode(keystoreFile);
    parentNode.addChild(keystoreNode);
  }

  private static void setInstance(KeyStoreView newInstance) {
    instance = newInstance;
    if (monitor == null) {
      monitor = new FileChangeMonitor(GeneralPreferencePage.getFilechangeMonitorInterval(), 1);
      monitor.addFileChangeListener(newInstance);
    }
  }
}

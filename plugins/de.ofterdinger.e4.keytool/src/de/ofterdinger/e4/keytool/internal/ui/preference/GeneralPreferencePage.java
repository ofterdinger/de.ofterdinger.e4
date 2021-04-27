package de.ofterdinger.e4.keytool.internal.ui.preference;

import de.ofterdinger.e4.keytool.internal.KeytoolPlugin;
import de.ofterdinger.e4.keytool.internal.certificate.KeystoreType;
import de.ofterdinger.e4.keytool.internal.ui.util.TextConstants;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class GeneralPreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {
  private static final int NO_OF_COLUMNS = 3;
  public static final String KEYSTORE_FILE = "prefs_keystore_filename";
  public static final String KEYSTORE_TYPE = "prefs_keystore_type";
  public static final String KEYSTORE_PASS = "prefs_keystore_password";
  public static final String FILECHANGE_MONITOR_INTERVAL = "prefs_keystore_filechange_interval";
  private static GeneralPreferencePage instance = null;
  private IWorkbench workbench;

  public GeneralPreferencePage() {
    super(1);
    setPreferenceStore(KeytoolPlugin.getDefault().getPreferenceStore());
    setDescription("Customize the keytool.");
  }

  @Override
  public void init(IWorkbench iWorkbench) {
    this.workbench = iWorkbench;
    setPreferenceStore(KeytoolPlugin.getDefault().getPreferenceStore());
  }

  @Override
  protected void createFieldEditors() {
    this.workbench.getActiveWorkbenchWindow();
    PlatformUI.getWorkbench()
        .getHelpSystem()
        .setHelp(getFieldEditorParent(), TextConstants.CERTIFICATE_EDITOR_HELP_ID);
    new SpacerFieldEditor(getFieldEditorParent());
    LabelFieldEditor label =
        new LabelFieldEditor(
            "Set interval in milliseconds between checks to see if an open keystore has changed.",
            getFieldEditorParent());
    label.adjustForNumColumns(NO_OF_COLUMNS);
    IntegerFieldEditor fileChangeMonitorInterval =
        new IntegerFieldEditor(
            FILECHANGE_MONITOR_INTERVAL, "Filechange monitor interval", getFieldEditorParent());
    addField(fileChangeMonitorInterval);
    createLine();
    label =
        new LabelFieldEditor(
            "Here you may specify a keystore that will be automatically loaded when Eclipse starts.",
            getFieldEditorParent());
    label.adjustForNumColumns(3);
    FileFieldEditor keystoreFile =
        new FileFieldEditor(
            KEYSTORE_FILE, "Keystore to be loaded automatically", true, getFieldEditorParent());
    addField(keystoreFile);
    StringFieldEditor keystorePassword =
        new StringFieldEditor(KEYSTORE_PASS, "Keystore password", getFieldEditorParent());
    keystorePassword.getTextControl(getFieldEditorParent()).setEchoChar('*');
    addField(keystorePassword);
    RadioGroupFieldEditor keystoreType =
        new RadioGroupFieldEditor(
            KEYSTORE_TYPE,
            "Keystore type",
            1,
            new String[][] {
              {"Java keystore (JKS)", KeystoreType.JKS.getType()},
              {"PKCS 12", KeystoreType.PKCS12.getType()}
            }, //$NON-NLS-2$
            getFieldEditorParent(),
            true);
    addField(keystoreType);
  }

  private void createLine() {
    Label line = new Label(getFieldEditorParent(), 259);
    GridData gridData = new GridData(768);
    gridData.horizontalSpan = 3;
    line.setLayoutData(gridData);
  }

  public static int getFilechangeMonitorInterval() {
    return GeneralPreferencePage.getPrefStore().getInt(FILECHANGE_MONITOR_INTERVAL);
  }

  public static String getKeystoreFile() {
    return GeneralPreferencePage.getPrefStore().getString(KEYSTORE_FILE);
  }

  public static String getKeystorePassword() {
    return GeneralPreferencePage.getPrefStore().getString(KEYSTORE_PASS);
  }

  public static KeystoreType getKeystoreType() {
    return KeystoreType.getInstance(GeneralPreferencePage.getPrefStore().getString(KEYSTORE_TYPE));
  }

  private static GeneralPreferencePage getInstance() {
    if (instance == null) {
      instance = new GeneralPreferencePage();
    }
    return instance;
  }

  private static IPreferenceStore getPrefStore() {
    return GeneralPreferencePage.getInstance().getPreferenceStore();
  }
}

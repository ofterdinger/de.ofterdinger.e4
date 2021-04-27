package de.ofterdinger.e4.keytool.internal.ui.preference;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import de.ofterdinger.e4.keytool.internal.KeytoolPlugin;
import de.ofterdinger.e4.keytool.internal.certificate.KeystoreType;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer {
  public static final int DEFAULT_FILECHANGE_MONITOR_INTERVAL = 60000;

  @Override
  public final void initializeDefaultPreferences() {
    IPreferenceStore defaults = KeytoolPlugin.getDefault().getPreferenceStore();
    defaults.setDefault(GeneralPreferencePage.KEYSTORE_FILE, EMPTY);
    defaults.setDefault(GeneralPreferencePage.KEYSTORE_TYPE, KeystoreType.JKS.getType());
    defaults.setDefault(GeneralPreferencePage.KEYSTORE_PASS, EMPTY);
    defaults.setDefault(
        GeneralPreferencePage.FILECHANGE_MONITOR_INTERVAL, DEFAULT_FILECHANGE_MONITOR_INTERVAL);
  }
}

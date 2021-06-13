package de.ofterdinger.ide.eclipse.keytool.internal.ui.preference;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import de.ofterdinger.ide.eclipse.keytool.internal.KeytoolPlugin;
import de.ofterdinger.ide.eclipse.keytool.internal.certificate.KeystoreType;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

  @Override
  public final void initializeDefaultPreferences() {
    IPreferenceStore defaults = KeytoolPlugin.getDefault().getPreferenceStore();
    defaults.setDefault(GeneralPreferencePage.KEYSTORE_FILE, EMPTY);
    defaults.setDefault(GeneralPreferencePage.KEYSTORE_TYPE, KeystoreType.JKS.getType());
    defaults.setDefault(GeneralPreferencePage.KEYSTORE_PASS, EMPTY);
    defaults.setDefault(GeneralPreferencePage.FILECHANGE_MONITOR_INTERVAL, 60000);
  }
}

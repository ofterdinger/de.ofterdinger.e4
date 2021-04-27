package de.ofterdinger.e4.keytool.internal.ui.util;

import org.eclipse.swt.widgets.Combo;

import de.ofterdinger.e4.keytool.internal.certificate.KeystoreType;

public final class KeystoreUIHelper {

	private KeystoreUIHelper() {
		// avoid instances
	}

	public static void populateAvailableKeystoreTypes(Combo keystoreTypeCombo) {
		keystoreTypeCombo.removeAll();
		int i = 0;
		while (i < KeystoreType.getTypes().length) {
			keystoreTypeCombo.add(KeystoreType.getTypes()[i].getType());
			if (i == 0) {
				keystoreTypeCombo.setText(KeystoreType.getTypes()[i].getType());
			}
			++i;
		}
	}
}

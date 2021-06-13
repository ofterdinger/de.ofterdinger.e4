package de.ofterdinger.ide.eclipse.keytool.internal.ui;

import static de.ofterdinger.ide.eclipse.keytool.internal.ui.util.TextConstants.KEYSTORE_VIEW_ID;
import static org.eclipse.ui.IPageLayout.LEFT;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class KeytoolPerspectiveFactory implements IPerspectiveFactory {
  private static final float RELATIVE_LOCATION = 0.26f;

  @Override
  public void createInitialLayout(IPageLayout layout) {
    layout.setEditorAreaVisible(true);
    layout.setFixed(true);
    layout.addStandaloneView(
        KEYSTORE_VIEW_ID, true, LEFT, RELATIVE_LOCATION, layout.getEditorArea());
  }
}

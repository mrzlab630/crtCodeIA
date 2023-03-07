import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;



public class CrtCodeIASettings extends AnAction {



    private CrtCodeIASettingsState settingsState;

    public CrtCodeIASettings() {
        super("CrtCodeIASettings");
        settingsState = CrtCodeIASettingsState.getInstance();
    }



    @Override
    public void actionPerformed(AnActionEvent e) {

        CrtCodeIASettingsDialog dialog = new CrtCodeIASettingsDialog();

        dialog.showAndGet();


    }

}
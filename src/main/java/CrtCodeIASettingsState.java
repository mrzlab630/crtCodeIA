import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "CrtCodeIASettingsState",
        storages = {@Storage("CrtCodeIASettingsState.xml")}
)


public class CrtCodeIASettingsState implements PersistentStateComponent<CrtCodeIASettingsState> {

    private String url = "https://api.openai.com/v1/chat/completions";
    private String token = "";
    private String model = "gpt-3.5-turbo-0301";
    private String role = "you are a senior level programmer";

    @Nullable
    @Override
    public CrtCodeIASettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull CrtCodeIASettingsState state) {
        url = state.url;
        token = state.token;
        model = state.model;
        role = state.role;
    }

    public static CrtCodeIASettingsState getInstance() {
        return com.intellij.openapi.application.ApplicationManager.getApplication().getService(CrtCodeIASettingsState.class);
    }

    public String getUrl() {
        return url;
    }

    public String getToken() {
        return token;
    }

    public String getModel() {
        return model;
    }

    public String getRole() {
        return role;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
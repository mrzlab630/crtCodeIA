import com.google.gson.*;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.command.WriteCommandAction;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.google.gson.JsonArray;
import org.apache.http.util.EntityUtils;


public class CrtCodeIA extends AnAction {

    CrtCodeIASettingsState state;
    Document document;
    String documentText;
    String targetPrefix;
    public CrtCodeIA() {
        super("CrtCodeIA");
        state = CrtCodeIASettingsState.getInstance();
        targetPrefix = "//crtCodeIA";
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        String url = state.getUrl();
        String token = state.getToken();
        String model = state.getModel();
        String role = state.getRole();

        String textToCopy = "";


        final Project project = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);

        final CaretModel caretModel = editor.getCaretModel();
        Document document = editor.getDocument();
        int caretOffset = caretModel.getOffset() + 1;
        documentText = document.getText();
        int index = documentText.indexOf(targetPrefix);
        if (index != -1) {
            int lineStart = documentText.lastIndexOf('\n', index) + 1;
            int lineEnd = documentText.indexOf('\n', index);
            if (lineEnd == -1) {
                lineEnd = documentText.length();
            }
            textToCopy = documentText.substring(lineStart, lineEnd);
        }

        final String content = textToCopy.replaceFirst(targetPrefix, "");

        if(content.length() == 0){
            notification(
                    "mrzlab630.creatCodeIA.notification",
                    "CrtCodeIA",
                    "crtCodeIA is empty"
            ).notify(project);
            return;
        }

        String requestParams = requestParams(role,content,model);

        Thread requestThread = new Thread(() -> {
            try {
                if(token == null || token.length() == 0){
                    throw new IllegalArgumentException("token is empty");
                }
                WriteCommandAction.runWriteCommandAction(project, () -> document.setText(documentText.replace(targetPrefix, targetPrefix+":Waiting...")));

                notification(
                        "mrzlab630.creatCodeIA.notification",
                        "CrtCodeIA",
                        "Waiting..."
                ).notify(project);

                String codeIA = request(url,token,requestParams);


                WriteCommandAction.runWriteCommandAction(project, () -> {
                            document.setText(documentText.replace(targetPrefix, targetPrefix + ":DONE"));
                            document.insertString(caretOffset, "\n/**\n" + codeIA + "\n*/");
                        });

                notification(
                        "mrzlab630.creatCodeIA.notification",
                        "CrtCodeIA",
                        "Done"
                ).notify(project);

            }catch (Exception ex){
                String errorMessage = ex.getMessage();

                WriteCommandAction.runWriteCommandAction(project, () -> {
                    document.setText(documentText.replace(targetPrefix, targetPrefix + ":Error"));
                    document.insertString(caretOffset, "\n/**\n\nError:\n" + errorMessage + "\n\n*/");
                });


               notification(
                        "mrzlab630.creatCodeIA.notification",
                        "CrtCodeIA",
                        errorMessage
                ).notify(project);

            }
        });

        requestThread.start();
    }


    public  Notification notification(
            String groupId,
            String title,
            String text
    ){
        Notification notification = new Notification(
                groupId,
                title,
                text,
                NotificationType.INFORMATION
        );

        return notification;
    }


    public String request(String url, String token, String requestParams){

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            HttpPost httpPost = new HttpPost(url);


            String apiKey ="Bearer " + token;
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, apiKey);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            HttpEntity requestEntity = new StringEntity(requestParams, ContentType.APPLICATION_JSON);
            httpPost.setEntity(requestEntity);

            CloseableHttpResponse response = httpClient.execute(httpPost);

            HttpEntity responseEntity = response.getEntity();
            String responseString = EntityUtils.toString(responseEntity);
            JsonObject responseJson = gson.fromJson(responseString, JsonObject.class);
            JsonArray choices = responseJson.getAsJsonArray("choices");
            JsonObject error = responseJson.getAsJsonObject("error");

            String contentRes = "";

            if (error != null && error.size() > 0) {
                contentRes =  "Error:\n" + error.get("message").getAsString();
            }

            if (choices != null && choices.size() > 0) {
                JsonObject choice = choices.get(0).getAsJsonObject();
                JsonObject message = choice.get("message").getAsJsonObject();
                contentRes = message.get("content").getAsString();
            }

            final String finalContentRes = contentRes;
            return finalContentRes;

        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
            return "Error:\n" + errorMessage;
        }
    }

    public String requestParams(
            String role,
            String content,
            String model
    ){

        JsonObject message1 = new JsonObject();
        message1.addProperty("role", "system");
        message1.addProperty("content", role);

        JsonObject message2 = new JsonObject();
        message2.addProperty("role", "user");
        message2.addProperty("content", content);

        JsonArray messages = new JsonArray();
        messages.add(message1);
        messages.add(message2);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("model", model);
        jsonObject.add("messages", messages);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(jsonObject);

        return json;
    }

    @Override
    public void update(AnActionEvent e) {
        final Project project = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabledAndVisible(project != null && editor != null);
    }

}




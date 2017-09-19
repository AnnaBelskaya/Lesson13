import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import youtube.YouTubeVideoPlayer;
import youtube.entities.Activity;
import youtube.entities.ActivityResponse;

import java.io.IOException;

public class Program extends Application {
    private static String URL = "http://www.youtube.com/embed/";
    private static String AUTO_PLAY = "?autoplay=1";

    private static void initApplication() {
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    public static void main(String[] args) throws UnirestException {
        initApplication();
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        final Pane root = new Pane();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        final TextField channelId = new TextField("UCjamiTU85WjDKkfgsGSqi7w");
        channelId.setTranslateX(150);
        channelId.setTranslateY(60);

        final TextField maxResults = new TextField("5");
        maxResults.setTranslateX(150);
        maxResults.setTranslateY(110);

        Button initButton = new Button("Init");
        initButton.setTranslateX(150);
        initButton.setTranslateY(10);
        initButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                try {
                    getActivity(root, channelId.getText(), maxResults.getText());
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
            }
        });

        root.getChildren().addAll(initButton, channelId, maxResults);
        primaryStage.setHeight(500);
        primaryStage.setWidth(800);
        primaryStage.show();
    }

    private void getActivity(final Pane root, String channelId, String maxResults) throws UnirestException {
        HttpResponse<ActivityResponse> response = Unirest.get("https://www.googleapis.com/youtube/v3/activities")
                .queryString("part", "contentDetails")
                .queryString("channelId", channelId)
                .queryString("maxResults", maxResults)
                .queryString("key", "AIzaSyCT5uQTJSRDdTZJXVDm30wUsii3oNNa11Q")
                .asObject(ActivityResponse.class);

        ActivityResponse activity = response.getBody();

        for(int i = 0; i < activity.items.size(); i++) {
            final Activity item = activity.items.get(i);
            if (item.contentDetails.upload == null) continue;

            Button button = new Button("Video " + i);
            button.setTranslateX(10);
            button.setTranslateY(50 * i + 10);
            root.getChildren().addAll(button);

            button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent event) {
                    String videoId = item.contentDetails.upload.videoId;
                    System.out.println(videoId);

                    final WebView webview = new WebView();
                    webview.getEngine().load(
                            URL+videoId+AUTO_PLAY
                    );
                    webview.setPrefSize(640, 390);

                    final Button close = new Button("close");
                    close.setTranslateX(webview.getWidth() + 10);
                    close.setTranslateY(10);
                    close.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        public void handle(MouseEvent event) {
                            root.getChildren().remove(close);
                            root.getChildren().remove(webview);
                        }
                    });

                    root.getChildren().addAll(webview, close);
                }
            });
        }
    }
}

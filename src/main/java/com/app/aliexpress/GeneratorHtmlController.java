package com.app.aliexpress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.app.util.AliExpress;
import com.app.util.Crawling;
import com.app.util.Editor;
import com.app.util.EditorType;
import com.app.util.LinkType;
import com.app.util.WebDriverPool;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;

public class GeneratorHtmlController {

	@FXML
	private AnchorPane root;
	@FXML
	private TextField tfKeyword;
	@FXML
	private Button btnGenerator;
	@FXML
	private Label lbCopy;
	@FXML
	private TextArea taHtml;

	// java FX Thread에 넣기 위해 사용
	private Alert doingAlert;

	@FXML
	private void initialize() {
		Platform.runLater(() -> {
			root.requestFocus();
			taHtml.setEditable(false);
			lbCopy.setVisible(false);
		});
	}

	@FXML
	private void handleKeywordAction() {
		handleGeneratorClick();
	}
	
	@FXML
	private void handleGeneratorClick() {
		
		// 키워드 가져오기
		String keyword = tfKeyword.getText().trim();
		
		if (keyword.equals("")) {
			Platform.runLater(() -> {
				String content = "키워드 입력 후, 생성 버튼을 클릭해 주세요.";
				
				Alert alert = getDoingAlert();
				alert.setAlertType(AlertType.ERROR);
				alert.setContentText(content);
				alert.show();
				
				setDoingAlert(null);
				
				tfKeyword.requestFocus();
			});
			
			return;
		}
		
		Platform.runLater(() -> {
			
			String content = "크롤링 중 입니다. 잠시만 기다려 주세요.";
			
			Alert alert = getDoingAlert();
			alert.setContentText(content);
			
			alert.show();
			
		});
		
		Thread parentThread = new Thread(new Runnable() {

			@Override
			public void run() {
				WebDriverPool webDriverPool = WebDriverPool.getInstance();
				Crawling crawling = webDriverPool.getCrawling();
				
				List<Properties> crawlingList = crawling.getContents(keyword);

				// 크롤링 완료 후 alert 변경
				Platform.runLater(() -> {
					String content = "링크 변환 준비 중입니다. 잠시만 기다려 주세요.";

					Alert alert = getDoingAlert();
					alert.setContentText(content);

					if (!alert.isShowing()) {
						alert.show();
					}
				});

				// promotion_link로 변환
				// 새로 담을 map
				Map<String, List<Properties>> processedData = new HashMap<>();
				
				// 새로 담을 list
				List<Properties> processedProps = new ArrayList<>();
				
				int contentCount = crawlingList.size();

				for (int i = 0; i < contentCount; i++) {
					
					if (processedProps.size() == 10) {
						break;
					}
					
					// thread 특성때문에 변수에 저장
					int nowCount = i;
					
					Properties prop = crawlingList.get(i);
					
					Platform.runLater(() -> {

						String content = nowCount + " / " + contentCount + " 변환 완료";

						Alert alert = getDoingAlert();
						alert.setContentText(content);
						
						if (!alert.isShowing()) {
							alert.show();
						}
					});

					String link = prop.getProperty("link");

					try {
						link = AliExpress.linkGenerate(LinkType.PRODUCT, link);
						
						if (link == null) continue;
						
						prop.put("link", link);
						processedProps.add(prop);

					} catch (Exception e) {
						e.printStackTrace();
						// 생성 실패한 링크
					}
				}

				processedData.put(keyword, processedProps);
				
				try {
					Platform.runLater(() -> {
						String content = "HTML 생성 중 입니다. 잠시만 기다려 주세요.";

						Alert alert = getDoingAlert();
						alert.setContentText(content);
						
						if (!alert.isShowing()) {
							alert.show();
						}
					});
					
					String html = (String) Editor.createHtml(EditorType.HTML, keyword, processedData).get("content");
					
					Platform.runLater(() -> {
						String content = "HTML 생성을 완료하였습니다.";

						Alert alert = getDoingAlert();
						alert.setContentText(content);
						
						if (!alert.isShowing()) {
							alert.show();
						}
						
						taHtml.setText(html);
						lbCopy.setText("Click below to copy to clipboard");
						lbCopy.setTextFill(Color.BLACK);
						lbCopy.setVisible(true);
					});
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		parentThread.setDaemon(true);
		parentThread.start();
		
	}
	
	@FXML
	private void handleHtmlClick() {
		ClipboardContent content = new ClipboardContent();
		content.putString(taHtml.getText());
		Clipboard.getSystemClipboard().setContent(content);
		
		Platform.runLater(() -> {
			lbCopy.setText("Copied to clipboard!");
			lbCopy.setTextFill(Color.web("#088A29"));
		});
	}

	// getter, setter
	public Alert getDoingAlert() {

		if (doingAlert == null) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Html Generator");
			alert.setHeaderText(null);
			alert.initModality(Modality.NONE);

			setDoingAlert(alert);
		}

		return doingAlert;
	}

	public void setDoingAlert(Alert doingAlert) {
		this.doingAlert = doingAlert;
	}
}

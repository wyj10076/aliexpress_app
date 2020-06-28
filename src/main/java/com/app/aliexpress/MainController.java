package com.app.aliexpress;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import com.app.dto.UserDTO;
import com.app.util.AliExpress;
import com.app.util.Config;
import com.app.util.Crawling;
import com.app.util.Editor;
import com.app.util.EditorType;
import com.app.util.FileConverter;
import com.app.util.LinkType;
import com.app.util.MailSender;
import com.app.util.WebDriverPool;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class MainController {

	@FXML
	private AnchorPane root;
	@FXML
	private TableView<UserDTO> table;
	@FXML
	private TableColumn<?, ?> colNum;
	@FXML
	private TableColumn<?, ?> colEmail;
	@FXML
	private TableColumn<?, ?> colFirstKeyword;
	@FXML
	private TableColumn<?, ?> colSecondKeyword;
	@FXML
	private TextField tfEmail;
	@FXML
	private TextField tfFirstKeyword;
	@FXML
	private TextField tfSecondKeyword;
	@FXML
	private Button btnHtml;
	@FXML
	private Button btnPreview;
	@FXML
	private Button btnSendMail;

	private MailSender sender;

	private Map<String, List<Properties>> crawlingData;
	
	private Stage modalStage;
	// java FX Thread에 넣기 위해 사용
	private Alert doingAlert;
	// Thread 안 에서 count 변경이 불가능 하여 사용
	private int nowCount;
	// 변경사항 유무 체크
	private boolean isChange;
	// 메일 체크
	private boolean isExistEmail;
	// 파일 유무 체크
	private boolean hasFile;
	
	@FXML
	private void initialize() {
		mailCheck(Config.getEmail(), Config.getPassword());
		fileCheck();

		if (!isExistEmail()) {
			btnSendMail.setDisable(true);
		}
		if (!isHasFile()) {
			btnHtml.setDisable(true);
			btnPreview.setDisable(true);
			btnSendMail.setDisable(true);
		}
		
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		colNum.setCellValueFactory(new PropertyValueFactory<>("num"));
		colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		colFirstKeyword.setCellValueFactory(new PropertyValueFactory<>("firstKeyword"));
		colSecondKeyword.setCellValueFactory(new PropertyValueFactory<>("secondKeyword"));
		
		// 쓰레드로 감싸줘야 제대로 동작
		Platform.runLater(() -> root.requestFocus());
	}
	
	@FXML
	private void handleEmailAction() {
		handleAddClick();
	}
	
	@FXML
	private void handleFirstKeywordAction() {
		handleAddClick();
	}
	
	@FXML
	private void handleSecondKeywordAction() {
		handleAddClick();
	}
	
	@FXML
	private void handleAddClick() {

		String email = tfEmail.getText().trim();
		String firstKeyword = tfFirstKeyword.getText().trim();
		String secondKeyword = tfSecondKeyword.getText().trim();

		if (email.equals("")) {
			Platform.runLater(() -> {
				String content = "이메일은 필수 입력란 입니다.";
				
				Alert alert = createErrorAlert(content);
				alert.show();
				
				tfEmail.requestFocus();
			});
			
			return;
			
		} else if (firstKeyword.equals("")) {
			Platform.runLater(() -> {
				String content = "키워드1은 필수 입력란 입니다.";
				
				Alert alert = createErrorAlert(content);
				alert.show();
				
				tfFirstKeyword.requestFocus();
			});
			
			return;
		}
		
		setChange(true);
		Integer nextNum = table.getItems().size() + 1;
		UserDTO user = new UserDTO(nextNum, email, firstKeyword, secondKeyword);
		table.getItems().add(user);
		tfEmail.clear();
		tfFirstKeyword.clear();
		tfSecondKeyword.clear();
		tfEmail.requestFocus();
		
		if (getCrawlingData() != null) {
			setCrawlingData(null);
		}
	}
	
	@FXML
	private void handleRemoveClick() {
		ObservableList<UserDTO> users = table.getSelectionModel().getSelectedItems();
		// Row Not selected.
		if (users.size() == 0) {
			String content = "선택된 항목이 없습니다.";

			Alert alert = createErrorAlert(content);
			alert.show();

			return;
		}
		
		setChange(true);
		table.getItems().removeAll(users);
		table.getSelectionModel().clearSelection();
		
		if (getCrawlingData() != null) {
			setCrawlingData(null);
		}
	}
	
	// option
	@FXML
	private void handleHtmlClick(ActionEvent event) {
		try {
			Stage modal = generateStage(event);
			
			if (modal.isShowing()) {
				modal.hide();
				
			} else {
				Window nowWindow = root.getScene().getWindow();
				double x = nowWindow.getX() - 252;
				double y = nowWindow.getY() + nowWindow.getHeight() - 330;
				
				modal.setX(x);
				modal.setY(y);
				
				modal.show();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// mail
	@FXML
	private void handlePreviewClick() {
		
		if (table.getItems().size() == 0) {
			String content = "테이블에 항목이 없습니다.";

			Alert alert = createErrorAlert(content);
			alert.show();
			
			return;
		}
		
		Thread parentThread = new Thread(new Runnable() {

			@Override
			public void run() {
				Map<String, List<Properties>> crawlingData = generateData();
				StringBuilder str = new StringBuilder();
				crawlingData.forEach((key, value) -> {
					try {
						str.append(Editor.createHtml(EditorType.PREVIEW, key, crawlingData).get("content"));
						str.append("<br/><br/>");
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				
				Platform.runLater(() -> {
					
					String content = "임시 파일 생성 중입니다.";
					
					Alert alert = getDoingAlert();
					alert.setContentText(content);
					
					if (alert.isShowing()) {
						alert.show();
					}
				});
				
				try {
					String tmpPath = Config.getTempFilePath();
					File dir = new File(tmpPath);
					
					if (!dir.exists()) {
						dir.mkdirs();
					}
					
					File tmpFile;
					tmpFile = File.createTempFile("preview_", ".html", dir);

					// Delete tmp file
					tmpFile.deleteOnExit();
					
					OutputStream output = new FileOutputStream(tmpFile);
					byte[] by=str.toString().getBytes();
					
					output.write(by);
					output.close();
					
					Platform.runLater(() -> {
						
						String content = tmpFile.getPath() + " 생성 완료";
						
						Alert alert = getDoingAlert();
						alert.setContentText(content);
						
						if (alert.isShowing()) {
							alert.show();
						}
						
						// doingAlert 정리
						setDoingAlert(null);
					});
					
					Desktop.getDesktop().browse(new URI("file:///" + tmpFile.getPath().replaceAll("\\\\", "/")));

				} catch (Exception e) {
					e.printStackTrace();
				}
			    
				
			}
		});
		
		parentThread.setDaemon(true);
		parentThread.start();

	}

	@FXML
	private void handleSendMailClick() {
		ObservableList<UserDTO> users = table.getItems();

		if (users.size() == 0) {
			String content = "테이블에 항목이 없습니다.";

			Alert alert = createErrorAlert(content);
			alert.show();
			
			return;
		} 
		
		long start = System.currentTimeMillis();
		Thread parentThread = new Thread(new Runnable() {

			@Override
			public void run() {

				Map<String, List<Properties>> crawlingData = generateData();

				// 크롤링 완료 후 alert 변경
				Platform.runLater(() -> {

					String content = "링크 변환 준비 중입니다.";

					Alert alert = getDoingAlert();
					alert.setContentText(content);

					if (!alert.isShowing()) {
						alert.show();
					}
				});

				// promotion_link로 변환
				// forEach 도는 중에 map을 변경하면 안될 것 같아서 새로 담을 map을 만들었음
				Map<String, List<Properties>> processedData = new HashMap<>();

				// 보여주기 위한 총 변환해야할 link 개수
				int keywordCount = crawlingData.size();
				setNowCount(0); // 초기값

				crawlingData.forEach((key, value) -> {
					List<Properties> processedProps = new ArrayList<>();
					List<Properties> props = value;
					int contentCount = props.size();

					for (Properties prop : props) {
						if (processedProps.size() >= 10) {
							break;
						}
						
						Platform.runLater(() -> {

							String content = getNowCount() + " / " + (keywordCount * contentCount) + " 변환 완료";

							Alert alert = getDoingAlert();
							alert.setContentText(content);
						});

						String link = prop.getProperty("link");
						
						try {
							link = AliExpress.linkGenerate(LinkType.PRODUCT, link);
							
							// Thread 안에서 지역 변수는 final 값이기 때문에 class의 멤버 필드를 사용
							setNowCount(getNowCount() + 1);
							
							if (link == null) continue;
							
							prop.put("link", link);
							processedProps.add(prop);

						} catch (Exception e) {
							e.printStackTrace();
							// 생성 실패한 링크
						}
					}

					processedData.put(key, processedProps);
				});

				Platform.runLater(() -> {

					String content = "메일 전송 준비 중입니다.";

					Alert alert = getDoingAlert();
					alert.setContentText(content);

					if (!alert.isShowing()) {
						alert.show();
					}
				});

				// 메일 보내기
				try {
					sender.connect();

					for (int i = 0; i < users.size(); i++) {
						int nowSendingCount = i;

						UserDTO user = users.get(i);

						Platform.runLater(() -> {

							String content = nowSendingCount + " / " + users.size() + " 메일 전송 완료";

							Alert alert = getDoingAlert();
							alert.setContentText(content);
						});

						// 메일 전송
						sender.sendMessage(user, processedData);
					}

					sender.disconnect();

					Platform.runLater(() -> {

						long end = System.currentTimeMillis();
						
						String content = users.size() + "건의 메일 전송이 완료되었습니다.\n" + getConvertTime(end - start);

						Alert alert = getDoingAlert();
						alert.setContentText(content);

						if (!alert.isShowing()) {
							alert.show();
						}
						
						// doingAlert 정리
						setDoingAlert(null);
					});

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		parentThread.setDaemon(true);
		parentThread.start();
		
	}
	
	// file
	@FXML
	private void handleLoadClick(ActionEvent event) throws Exception {
		if (isChange() && table.getItems().size() != 0) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("알리 익스프레스");
			alert.setHeaderText(null);
			alert.setContentText("변경사항이 존재합니다. 저장하시겠습니까?");
			((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancel");
			
			Optional<ButtonType> clickButton = alert.showAndWait();
			if (clickButton.get() == ButtonType.OK) handleSaveClick(event);
			
			setChange(false);
			 
			alert.setContentText("파일을 불러오시겠습니까?");
			clickButton = alert.showAndWait();
			if (clickButton.get() != ButtonType.OK) return; 
		}
		
		
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extensionFilter = new ExtensionFilter("Excel File", "*.xls", "*.xlsx");
		fileChooser.getExtensionFilters().add(extensionFilter);
		Node source = (Node) event.getSource();
		
		File file = fileChooser.showOpenDialog(source.getScene().getWindow());

		if (file == null) return;

		String content = file.getName() + " 파일을 불러오는 중 입니다.";

		Platform.runLater(() -> {
			Alert alert = getDoingAlert();
			alert.setContentText(content);

			if (!alert.isShowing()) {
				alert.show();
			}
		});
		
		table.setItems(FileConverter.loadFile(file));

		if (getCrawlingData() != null) {
			setCrawlingData(null);
		}
		
		Platform.runLater(() -> {
			Alert alert = getDoingAlert();

			if (alert.isShowing()) {
				alert.close();
			}
		});
	}

	@FXML
	private void handleSaveClick(ActionEvent event) throws Exception {
		
		if (table.getItems().size() == 0) {
			Platform.runLater(() -> {
				String content = "저장할 내용이 없습니다.";
				
				Alert alert = createErrorAlert(content);
				alert.setContentText(content);
				
				alert.show();
			});
			return;
		}
		
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extensionFilter = new ExtensionFilter("Excel File", "*.xls", "*.xlsx");
		fileChooser.getExtensionFilters().add(extensionFilter);
		Node source = (Node) event.getSource();

		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		fileChooser.setInitialFileName(format.format(now) + ".xlsx");

		File file = fileChooser.showSaveDialog(source.getScene().getWindow());

		if (file == null) return;


		Platform.runLater(() -> {
			String content = file.getName() + " 파일을 저장 중 입니다.";

			Alert alert = getDoingAlert();
			alert.setContentText(content);
			
			if (!alert.isShowing()) {
				alert.show();
			}
		});

		FileConverter.saveFile(file, table.getItems());
		
		String content = file.getName() + " 파일 저장 완료";
		
		Alert alert = getDoingAlert();
		Platform.runLater(() -> {
			alert.setContentText(content);
		});
		
		if (!alert.isShowing()) {
			alert.showAndWait();
		}

	}
	
	// list
	@FXML
	private void handleSelectAllClick() {
		table.getSelectionModel().selectAll();
	}

	@FXML
	private void handleDeselectClick() {
		table.getSelectionModel().clearSelection();
	}

	private Alert createErrorAlert(String content) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("알리 익스프레스");
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.initModality(Modality.NONE);
		return alert;
	}
	
	private String getConvertTime(long milliseconds) {
		long hour = (milliseconds / (1000 * 60 * 60)) % 24;
		long minute = (milliseconds / (1000 * 60)) % 60;
		long second = (milliseconds / 1000) % 60;
		
		String convertTime = "소요시간 : ";
		convertTime += hour != 0 ? hour + "시간 " : "";
		convertTime += minute != 0 ? minute + "분 " : "";
		convertTime += second != 0 ? second + "초 " : "";
		
		return convertTime;
	}
	
	private Map<String, List<Properties>> generateData() {
		if (getCrawlingData() == null) {
			setCrawlingData(startCrawling());
		}
		
		return getCrawlingData();
	}
	
	private Map<String, List<Properties>> startCrawling() {
		// 1. 크롤링 alert 띄우기
		Platform.runLater(() -> {
			String content = "크롤링 준비 중입니다.";

			Alert alert = getDoingAlert();
			alert.setContentText(content);
			alert.show();
		});

		// 2. Crawling할 keywords 만들기
		List<String> keywords = new ArrayList<String>();
		List<UserDTO> users = table.getItems();

		for (UserDTO user : users) {
			String firstKeyword = user.getFirstKeyword();
			String secondKeyword = user.getSecondKeyword();

			// keyword가 빈칸이 아니고, keywords에 추가된 내용이 아닐 경우 add
			if (!firstKeyword.equals("") && !keywords.contains(firstKeyword)) {
				keywords.add(firstKeyword);
			}

			if (!secondKeyword.equals("") && !keywords.contains(secondKeyword)) {
				keywords.add(secondKeyword);
			}
		}

		// 3. WebDriverPool 생성
		WebDriverPool webDriverPool = WebDriverPool.getInstance();

		// 4. crawling한 내용 담을 map 생성, key = keyword, value = crawling.getContents()
		Map<String, List<Properties>> crawlingData = new HashMap<String, List<Properties>>();

		// 5. crawlingThread를 관리하기 위한 threadGroup
		ThreadGroup crawlingGroup = new ThreadGroup("crawling");

		// 6. thread 생성하여 병렬 크롤링
		for (String keyword : keywords) {
			Thread crawlingThread = new Thread(crawlingGroup, new Runnable() {

				@Override
				public void run() {

					try {
						// Object Pool 에서 crawling 얻어오기
						Crawling crawling = webDriverPool.getCrawling();
						// 크롤링한 내용 담기
						crawlingData.put(keyword, crawling.getContents(keyword));
						// crawling 점유 넘기기
						webDriverPool.release(crawling);

					} catch (Exception e) {
						e.printStackTrace();
						// 에러 난 키워드를 map이나 리스트에 담고 싶음
					}
				}
			});

			crawlingThread.setDaemon(true);
			crawlingThread.start();
		}

		// 크롤링 완료까지 대기하기
		int initialCount = crawlingGroup.activeCount();
		int saveCount = -1;
		while (crawlingGroup.activeCount() > 0) {
			int count = crawlingGroup.activeCount();

			try {
				Thread.sleep(3000);
				// 변경 시에만 alert가 변화도록 제어문 설정
				if (saveCount != count) {
					Platform.runLater(() -> {

						String content = (initialCount - count) + " / " + initialCount + " 크롤링 완료";

						Alert alert = getDoingAlert();
						alert.setContentText(content);

						if (!alert.isShowing()) {
							alert.show();
						}

					});
					saveCount = count;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return crawlingData;
	}
	
	private Stage generateStage(ActionEvent event) throws IOException {
		if (getModalStage() == null) {
			Stage stage = new Stage(StageStyle.UNIFIED);
		    Parent parent;
		    parent = FXMLLoader.load(MainApp.class.getResource("/view/GeneratorHtml.fxml"));
			
		    stage.setScene(new Scene(parent));
		    stage.setTitle("HTML 생성기");
		    stage.initModality(Modality.NONE);
		    stage.initOwner(((Node)event.getSource()).getScene().getWindow());
		    stage.setResizable(false);
		    stage.sizeToScene();
		    
		    setModalStage(stage);
		}
		
		return getModalStage();
	}
	
	private void mailCheck(String email, String password) {
		try {
			sender = new MailSender(email, password);
			setExistEmail(true);
			
		} catch (Exception e) {
			Alert alert = createErrorAlert("이메일 계정을 확인해 주세요.");
			setExistEmail(false);
			alert.showAndWait();
			return;
		}
	}
	
	private void fileCheck() {
		File driver = new File(Config.getDriverPath());
		File image = new File(Config.getImagePath());
		
		String content = "파일이 존재하지 않습니다.\n";
		
		if (!driver.exists()) {
			content += driver.getPath();
			
		} else if (!image.exists()) {
			content += image.getPath();
			
		}
			
		if (driver.exists() && image.exists()) {
			setHasFile(true);
			
		} else {
			Alert alert = createErrorAlert(content);
			setHasFile(false);
			alert.showAndWait();
			
		}
		
	}
	
	// getter, setter
	public Alert getDoingAlert() {
		
		if (doingAlert == null) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("알리 익스프레스");
			alert.setHeaderText(null);
			alert.initModality(Modality.NONE);
			
			setDoingAlert(alert);
		}
		
		return doingAlert;
	}

	public void setDoingAlert(Alert doingAlert) {
		this.doingAlert = doingAlert;
	}

	public int getNowCount() {
		return nowCount;
	}

	public void setNowCount(int nowCount) {
		this.nowCount = nowCount;
	}

	public Map<String, List<Properties>> getCrawlingData() {
		return crawlingData;
	}

	public void setCrawlingData(Map<String, List<Properties>> crawlingData) {
		this.crawlingData = crawlingData;
	}

	public boolean isChange() {
		return isChange;
	}

	public void setChange(boolean change) {
		this.isChange = change;
	}

	public Stage getModalStage() {
		return modalStage;
	}

	public void setModalStage(Stage modalStage) {
		this.modalStage = modalStage;
	}

	public boolean isHasFile() {
		return hasFile;
	}

	public void setHasFile(boolean hasFile) {
		this.hasFile = hasFile;
	}

	public boolean isExistEmail() {
		return isExistEmail;
	}

	public void setExistEmail(boolean isExistEmail) {
		this.isExistEmail = isExistEmail;
	}

}

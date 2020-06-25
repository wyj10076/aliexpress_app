package com.app.aliexpress;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javax.mail.MessagingException;

import com.app.api.Crawling;
import com.app.api.FileConverter;
import com.app.api.MailSender;
import com.app.dto.UserDTO;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;

public class MainController implements Initializable {

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

	private MailSender sender;
	
	// java FX Thread에 넣기 위해 사용
	private Alert sendingAlert;
	
	@FXML
	private void handleAddClick() {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				Crawling.getContents("사이다");
			};
		}).start();
		
		String email = tfEmail.getText().trim();
		String firstKeyword = tfFirstKeyword.getText().trim();
		String secondKeyword = tfSecondKeyword.getText().trim();
		
		if (email.equals("") && firstKeyword.equals("")) {
			String title = "입력 경고";
			String header = "빈 칸이 존재합니다.";
			String content = "이메일과 키워드1은 필수 입력란 입니다.";
			
			Alert alert = createAlert(AlertType.WARNING, title, header, content);
			alert.show();
			
		} else {
			Integer nextNum = table.getItems().size() + 1;
			UserDTO user = new UserDTO(nextNum, email, firstKeyword, secondKeyword);
			table.getItems().add(user);
			tfEmail.clear();
			tfFirstKeyword.clear();
			tfSecondKeyword.clear();
			tfEmail.requestFocus();
		}
	}

	@FXML
	private void handleSelectAllClick() {
		table.getSelectionModel().selectAll();
	}
	
	@FXML
	private void handleDeselectClick() {
		table.getSelectionModel().clearSelection();
	}

	@FXML
	private void handleRemoveClick() {
		ObservableList<UserDTO> users = table.getSelectionModel().getSelectedItems();
		// Row Not selected.
		if (users.size() != 0) {
			table.getItems().removeAll(users);
			table.getSelectionModel().clearSelection();

		} else {
			String title = "삭제 경고";
			String header = "선택된 항목이 없습니다.";
			String content = "테이블에서 항목을 선택 후 삭제 버튼을 클릭해 주세요";
			
			Alert alert = createAlert(AlertType.WARNING, title, header, content);
			alert.show();
		}

	}

	@FXML
	private void handleSendMailClick() {
		
		if (table.getItems().size() == 0) {
			String title = "메일 전송 경고";
			String header = "전송할 메일이 없습니다.";
			String content = "항목을 먼저 입력 후, 메일 전송을 클릭해 주세요.";
			
			Alert alert = createAlert(AlertType.WARNING, title, header, content);
			alert.show();
			
		} else {
			new Thread(new Runnable() {

				@Override
				public void run() {
					
					Platform.runLater(() -> {
						String title = "전송 중";
						String header = "메일 전송 중";
						String content = "메일 전송 준비 중입니다. 종료하지 말아주세요.";
						
						Alert sending = createAlert(AlertType.WARNING, title, header, content);
						sending.initModality(Modality.NONE);
						sending.show();
						
						setSendingAlert(sending);
					});

					Thread childThread = new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								ObservableList<UserDTO> users = table.getItems();
								// 로그인
								sender.connect();
								
								for (int i = 0; i < users.size(); i++) {
									sender.sendMessage(users.get(i));
									
									int now = i + 1;
									Platform.runLater(() -> {
										getSendingAlert().setContentText(now + " / " + users.size() + " 메일 전송 완료");
									});
								}
								
							} catch (Exception e) {
								
								Platform.runLater(() -> {
									
									if (getSendingAlert().isShowing()) {
										getSendingAlert().close();
									}
									
									String title = "전송 실패";
									String header = "메일 전송 실패";
									String content = "전송 중 에러 발생\n" + e.toString();
									
									Alert alert = createAlert(AlertType.ERROR, title, header, content);
									alert.show();
								});
								
							} finally {
								//로그아웃
								try {
									sender.disconnect();
								} catch (MessagingException e) {
									e.printStackTrace();
								}
							}
						}
					});
					
					childThread.start();
					
					try {
						childThread.join();
						
					} catch (InterruptedException e) {/* Thread Error */}
					
					Platform.runLater(() -> {
						
						if (getSendingAlert().isShowing()) {
							getSendingAlert().close();
						}
						
						String title = "전송 완료";
						String header = "메일 전송 완료";
						String content = table.getItems().size() + "건 메일 전송";
						
						Alert alert = createAlert(AlertType.INFORMATION, title, header, content);
						alert.show();
					});

				}
			}).start();
		}

	}

	@FXML
	private void handleLoadClick(ActionEvent event) throws Exception {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extensionFilter = new ExtensionFilter("Excel File", "*.xls", "*.xlsx");
		fileChooser.getExtensionFilters().add(extensionFilter);
		Node source = (Node) event.getSource();

		File file = fileChooser.showOpenDialog(source.getScene().getWindow());
		
		if (file == null) return;
		
		String title = "파일 로드";
		String header = "파일 로드 중 입니다.";
		String content = file.getName() + " 파일 로드 중 입니다. 잠시만 기다려 주세요.";
		
		Alert loadingAlert = createAlert(AlertType.INFORMATION, title, header, content);
		loadingAlert.show();

		table.setItems(FileConverter.loadFile(file));
		
		if (loadingAlert.isShowing()) {
			loadingAlert.close();
		}
		
	}

	@FXML
	private void handleSaveClick(ActionEvent event) throws Exception {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extensionFilter = new ExtensionFilter("Excel File", "*.xls", "*.xlsx");
		fileChooser.getExtensionFilters().add(extensionFilter);
		Node source = (Node) event.getSource();

		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		fileChooser.setInitialFileName(format.format(now) + ".xlsx");

		File file = fileChooser.showSaveDialog(source.getScene().getWindow());

		if (file == null) return;
		
		String title = "파일 저장";
		String header = "파일 저장 중 입니다.";
		String content = file.getName() + " 파일 저장 중 입니다. 잠시만 기다려 주세요.";
		
		Alert loadingAlert = createAlert(AlertType.INFORMATION, title, header, content);
		loadingAlert.show();
		
		FileConverter.saveFile(file, table.getItems());
		
		if (loadingAlert.isShowing()) {
			loadingAlert.close();
		}

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			sender = new MailSender("qowlgh18@gmail.com", "Bae@ji06ho20");
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		colNum.setCellValueFactory(new PropertyValueFactory<>("num"));
		colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		colFirstKeyword.setCellValueFactory(new PropertyValueFactory<>("firstKeyword"));
		colSecondKeyword.setCellValueFactory(new PropertyValueFactory<>("secondKeyword"));
		Platform.runLater(() -> root.requestFocus());
	}
	
	private Alert createAlert(AlertType type, String title, String header, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		return alert;
	}

	// getter, setter
	public Alert getSendingAlert() {
		return sendingAlert;
	}

	public void setSendingAlert(Alert sendingAlert) {
		this.sendingAlert = sendingAlert;
	}
	
}

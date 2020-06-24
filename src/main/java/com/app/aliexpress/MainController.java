package com.app.aliexpress;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javax.mail.MessagingException;

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
	private Alert sendingAlert;
	
	@FXML
	private void handleAddClick() {
		Integer nextNum = table.getItems().size() + 1;
		UserDTO user = new UserDTO(nextNum, tfEmail.getText(), tfFirstKeyword.getText(), tfSecondKeyword.getText());
		table.getItems().add(user);
		Platform.runLater(() -> {
			tfEmail.clear();
			tfFirstKeyword.clear();
			tfSecondKeyword.clear();
			tfEmail.requestFocus();
		});
	}

	@FXML
	private void handleSelectAllClick() {
		table.getSelectionModel().selectAll();
	}

	@FXML
	private void handleRemoveClick() {
		ObservableList<UserDTO> users = table.getSelectionModel().getSelectedItems();
		// Row Not selected.
		if (users.size() != 0) {
			table.getItems().removeAll(users);
			table.getSelectionModel().clearSelection();

		} else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("No Selection");
			alert.setHeaderText("No User Selected");
			alert.setContentText("Please select a User in the table.");
			alert.showAndWait();
		}

	}

	@FXML
	private void handleSendMailClick() {
		Thread parentThread = new Thread(new Runnable() {

			@Override
			public void run() {
				
				Platform.runLater(() -> {
					Alert sending = new Alert(AlertType.WARNING);
					setSendingAlert(sending);
					
					sending.setTitle("전송 중");
					sending.setHeaderText("메일 전송 중");
					sending.setContentText("메일 전송 준비 중입니다. 종료하지 말아주세요.");
					sending.initModality(Modality.NONE);
					sending.show();
				});

				Thread t = new Thread(new Runnable() {

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
								
								Alert alert = new Alert(AlertType.ERROR);
								alert.setTitle("전송 실패");
								alert.setHeaderText("메일 전송 실패");
								alert.setContentText("전송 중 에러 발생\n" + e.toString());
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
				
				t.start();
				
				try {
					t.join();
					
				} catch (InterruptedException e) {/* Thread Error */}
				
				Platform.runLater(() -> {
					
					if (getSendingAlert().isShowing()) {
						getSendingAlert().close();
					}
					
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("전송 완료");
					alert.setHeaderText("메일 전송 완료");
					alert.setContentText(table.getItems().size() + "건 메일 전송");
					alert.show();
				});

			}
		});

		parentThread.start();
	}

	@FXML
	private void handleLoadClick(ActionEvent event) throws Exception {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extensionFilter = new ExtensionFilter("Excel File", "*.xls", "*.xlsx");
		fileChooser.getExtensionFilters().add(extensionFilter);
		Node source = (Node) event.getSource();

		File file = fileChooser.showOpenDialog(source.getScene().getWindow());

		table.setItems(FileConverter.loadFile(file));
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

		FileConverter.saveFile(file, table.getItems());

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

	public Alert getSendingAlert() {
		return sendingAlert;
	}

	public void setSendingAlert(Alert sendingAlert) {
		this.sendingAlert = sendingAlert;
	}
	
}

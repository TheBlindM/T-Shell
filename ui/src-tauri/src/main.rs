#![cfg_attr(
all(not(debug_assertions), target_os = "windows"),
windows_subsystem = "windows"
)]



use std::io::{Read, stdout};
use std::process::Command as StdCommand;
use std::{thread};
use std::os::windows::process::CommandExt;
use tauri::async_runtime::handle;
use tauri::{Manager, RunEvent};
use tauri::api::process::{Command as tauriCommand, CommandEvent};

#[tauri::command]
fn openFile(path: String) {
  if cfg!(target_os = "windows"){
  StdCommand::new("cmd").arg("/c").arg("start").arg(path).creation_flags(0x08000000).spawn().expect("cmd exec error!");
  }else{
   StdCommand::new("sh").arg("-c").arg("start").arg(path).creation_flags(0x08000000).spawn().expect("cmd exec error!");
  }

}
fn main() {
	let mut child = StdCommand::new("server.exe").creation_flags(0x08000000)
		.spawn().expect("cmd exec error!");
	tauri::Builder::default()
		.invoke_handler(tauri::generate_handler![openFile])
		.plugin(tauri_plugin_single_instance::init(|app, _argv, _cwd| { //设置插件
			let window = app.get_window("main").unwrap(); //二次打开软件时，显示已打开窗口，单例运行app
			window.set_focus().unwrap();
			window.show().unwrap();
		}))
		.build(tauri::generate_context!())
		.expect("err building")
		.run(move |handle, event| match event {
			tauri::RunEvent::Ready => {
				println!("程序启动")
			}
			tauri::RunEvent::Exit => {
				let client = reqwest::blocking::Client::new();
				let res = client.post("http://127.0.0.1:10218/system/shutdown")
					.send();
				println!("{:?}", res);
				child.kill().expect("!kill");
				println!("程序退出")
			}
			_ => ()
		});
}






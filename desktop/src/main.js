const { app, BrowserWindow, Menu } = require('electron');
const path = require('path');

function createWindow() {
  const win = new BrowserWindow({
    width: 1280,
    height: 720,
    minWidth: 800,
    minHeight: 500,
    backgroundColor: '#0E0F14',
    title: 'IPTV Player',
    autoHideMenuBar: true,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: false,
      webSecurity: false // cinerama HLS uchun CORS cheklovini olib tashlaydi
    }
  });
  Menu.setApplicationMenu(null);
  win.setContentProtection(true); // skrinshot / yozib olishdan himoya (Windows)
  win.loadFile(path.join(__dirname, 'index.html'));
}

const lock = app.requestSingleInstanceLock();
if (!lock) {
  app.quit();
} else {
  app.whenReady().then(createWindow);
  app.on('window-all-closed', () => app.quit());
}

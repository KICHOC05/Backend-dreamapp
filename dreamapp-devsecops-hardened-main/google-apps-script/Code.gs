function doPost(e) {
  try {
    const data = JSON.parse(e.postData.contents || "{}");
    const expectedSecret = PropertiesService.getScriptProperties().getProperty("APP_SHARED_SECRET");
    if (!expectedSecret || data.secret !== expectedSecret) {
      return jsonResponse({ success: false, error: "unauthorized" });
    }
    const recipient = String(data.recipient || "").trim().toLowerCase();
    const code = String(data.code || "").trim();
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(recipient) || !/^\d{6}$/.test(code)) {
      return jsonResponse({ success: false, error: "invalid_request" });
    }
    MailApp.sendEmail({
      to: recipient,
      subject: "Tu código de verificación de DreamApp",
      name: "DreamApp",
      body: "Tu código de verificación es: " + code +
        "\n\nCaduca en 10 minutos. Si no solicitaste esta cuenta, ignora este correo.\n\nDreamApp"
    });
    return jsonResponse({ success: true });
  } catch (error) {
    console.error(error);
    return jsonResponse({ success: false, error: "send_failed" });
  }
}

function jsonResponse(payload) {
  return ContentService.createTextOutput(JSON.stringify(payload))
    .setMimeType(ContentService.MimeType.JSON);
}

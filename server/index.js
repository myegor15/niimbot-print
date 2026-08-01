const express = require("express");
const sharp = require("sharp");
const { NiimbotNodeBleClient, NiimbotNodeSerialClient, LabelType } = require("@mmote/niimbluelib");
const { initClient, printImages, loadImageFromBase64, ImageEncoder } = require("@mmote/niimblue-node");

const PORT = process.env.PORT || 5010;
const DEBUG = process.env.DEBUG === "true";

let client = null;

const app = express();
app.use(express.json({ limit: "50mb" }));

const fail = (res, err, status) => res.status(status || 500).json({ message: err && err.message ? err.message : String(err) });

const assertConnected = () => {
  if (!client || !client.isConnected()) {
    const err = new Error("Not connected");
    err.status = 400;
    throw err;
  }
};

app.get("/", (req, res) => res.json({ message: "Server is working" }));

app.post("/connect", async (req, res) => {
  try {
    const { transport, address } = req.body || {};
    if (transport !== "ble" && transport !== "serial") {
      return fail(res, new Error("Invalid transport"), 400);
    }
    if (!address) {
      return fail(res, new Error("address is required"), 400);
    }
    if (client && client.isConnected()) {
      return fail(res, new Error("Already connected"), 400);
    }
    client = initClient(transport, address, DEBUG);
    await client.connect();
    res.json({ message: "Connected" });
  } catch (e) {
    fail(res, e);
  }
});

app.post("/disconnect", async (req, res) => {
  try {
    assertConnected();
    await client.disconnect();
    client = null;
    res.json({ message: "Disconnected" });
  } catch (e) {
    fail(res, e, e.status);
  }
});

app.get("/connected", (req, res) => {
  res.json({ connected: !!client && client.isConnected() });
});

app.get("/info", (req, res) => {
  try {
    assertConnected();
    res.json({
      printerInfo: client.getPrinterInfo(),
      modelMetadata: client.getModelMetadata(),
      detectedPrintTask: client.getPrintTaskType(),
    });
  } catch (e) {
    fail(res, e, e.status);
  }
});

app.get("/rfid", async (req, res) => {
  try {
    assertConnected();
    const paperRfidInfo = await client.abstraction.rfidInfo();
    let ribbonRfidInfo;
    try {
      ribbonRfidInfo = await client.abstraction.rfidInfo2();
    } catch (ignored) {
      // ribbon RFID is not supported by all models
    }
    res.json({ paperRfidInfo, ribbonRfidInfo });
  } catch (e) {
    fail(res, e, e.status);
  }
});

app.post("/scan", async (req, res) => {
  try {
    const transport = req.body && req.body.transport;
    if (transport === "ble") {
      const timeout = (req.body && req.body.timeout) || 5000;
      res.json({ devices: await NiimbotNodeBleClient.scan(timeout) });
    } else if (transport === "serial") {
      res.json({ devices: await NiimbotNodeSerialClient.scan() });
    } else {
      fail(res, new Error("Invalid transport"), 400);
    }
  } catch (e) {
    fail(res, e);
  }
});

app.post("/print", async (req, res) => {
  try {
    assertConnected();
    const options = req.body || {};

    if (!options.imageBase64) {
      return fail(res, new Error("imageBase64 is required"), 400);
    }

    const printTask = options.printTask || client.getPrintTaskType();
    if (!printTask) {
      return fail(res, new Error("Unable to detect print task, please set it manually"), 400);
    }

    const printDirection = options.printDirection
        || (client.getModelMetadata() && client.getModelMetadata().printDirection)
        || "left";

    let image = await loadImageFromBase64(options.imageBase64);
    image = image.flatten({ background: "#fff" });

    if (options.labelWidth && options.labelHeight) {
      image = image.resize(options.labelWidth, options.labelHeight, {
        kernel: sharp.kernel.nearest,
        fit: options.imageFit || "contain",
        position: options.imagePosition || "center",
        background: "#fff",
      });
    }
    image = image.threshold(options.threshold || 128);

    const encoded = await ImageEncoder.encodeImage(image, printDirection);
    const quantity = options.quantity || 1;

    await printImages(client, printTask, [{ encoded, quantity }], {
      quantity,
      labelType: options.labelType || LabelType.WithGaps,
      density: options.density || 3,
    });

    res.json({ message: "Printed" });
  } catch (e) {
    fail(res, e, e.status);
  }
});

app.listen(PORT, () => {
  console.log(`Server is listening :${PORT}`);
});

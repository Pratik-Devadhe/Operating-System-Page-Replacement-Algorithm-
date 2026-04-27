const express = require("express");
const bodyParser = require("body-parser");
const { exec } = require("child_process");

const app = express();

app.use(bodyParser.urlencoded({ extended: true }));
app.set("view engine", "ejs");

app.get("/", (req, res) => {
    res.render("index", { result: null });
});

app.post("/simulate", (req, res) => {
    const { sequence, frames, algorithm } = req.body;

    exec(`java PageFault "${sequence}" ${frames} ${algorithm}`, (err, stdout) => {
        if (err) return res.send("Error running Java");

        try {
            const result = JSON.parse(stdout);
            res.render("index", { result });
        } catch {
            console.log(stdout);
            res.send("Invalid JSON from Java");
        }
    });
});

app.listen(3000, () => console.log("Server running on port 3000"));
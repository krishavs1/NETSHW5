// graphBackend.js
const express = require('express');
const multer = require('multer');
const fs = require('fs');
const pdfParse = require('pdf-parse');
const cors = require('cors');

const app = express();
const PORT = 3000;
const upload = multer({ dest: 'uploads/' });

app.use(express.json());
app.use(cors());

// In-memory storage
const userProfiles = {}; // username -> [keywords]
const graph = {}; // username -> [connected usernames]

// Basic cosine similarity between two word arrays
function cosineSimilarity(wordsA, wordsB) {
  const allWords = new Set([...wordsA, ...wordsB]);
  const vecA = Array.from(allWords).map(w => wordsA.includes(w) ? 1 : 0);
  const vecB = Array.from(allWords).map(w => wordsB.includes(w) ? 1 : 0);
  const dot = vecA.reduce((sum, a, i) => sum + a * vecB[i], 0);
  const magA = Math.sqrt(vecA.reduce((sum, a) => sum + a * a, 0));
  const magB = Math.sqrt(vecB.reduce((sum, b) => sum + b * b, 0));
  return dot / (magA * magB);
}

// Update graph with new connections
function updateGraph(newUser) {
  const newKeywords = userProfiles[newUser];
  graph[newUser] = [];
  for (const other in userProfiles) {
    if (other !== newUser) {
      const sim = cosineSimilarity(newKeywords, userProfiles[other]);
      if (sim >= 0.3) { // similarity threshold
        graph[newUser].push(other);
        graph[other] = graph[other] || [];
        graph[other].push(newUser);
      }
    }
  }
}

// Parse resume and build profile
app.post('/api/upload-resume', upload.single('resumeFile'), async (req, res) => {
  const username = req.body.username;
  if (!username || !req.file) return res.status(400).send('Missing username or file');

  try {
    const dataBuffer = fs.readFileSync(req.file.path);
    const parsed = await pdfParse(dataBuffer);
    const words = parsed.text.toLowerCase().match(/\b\w{4,}\b/g) || []; // words 4+ letters

    userProfiles[username] = words;
    updateGraph(username);

    res.sendStatus(200);
  } catch (err) {
    console.error(err);
    res.sendStatus(500);
  }
});

// Query graph distance
function bfs(graph, from, to) {
  const queue = [[from]];
  const visited = new Set([from]);
  while (queue.length > 0) {
    const path = queue.shift();
    const node = path[path.length - 1];
    if (node === to) return { distance: path.length - 1, path };
    for (const neighbor of graph[node] || []) {
      if (!visited.has(neighbor)) {
        visited.add(neighbor);
        queue.push([...path, neighbor]);
      }
    }
  }
  return { distance: -1, path: [] };
}

app.get('/api/distance', (req, res) => {
  const { from, to } = req.query;
  if (!from || !to || !graph[from] || !graph[to]) {
    return res.status(400).json({ distance: -1, path: [] });
  }
  const result = bfs(graph, from, to);
  res.json(result);
});

app.listen(PORT, () => console.log(`Graph backend running at http://localhost:${PORT}`));

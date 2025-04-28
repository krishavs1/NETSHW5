// Handle “Compute Distance”
document.getElementById('distanceForm').addEventListener('submit', async e => {
    e.preventDefault();
    const from = document.getElementById('userA').value;
    const to   = document.getElementById('userB').value;
    // TODO: replace with your back-end URL:
    // const res = await fetch(`/api/distance?from=${from}&to=${to}`);
    // const data = await res.json();
    const data = { distance: 3, path: ['you','alice','bob','target'] }; // placeholder

    document.getElementById('distanceResult').innerHTML =
        `<p><strong>Distance:</strong> ${data.distance}</p>
     <p><strong>Path:</strong> ${data.path.join(' → ')}</p>`;
});

// Handle “Recommendations”
document.getElementById('recForm').addEventListener('submit', async e => {
    e.preventDefault();
    const user = document.getElementById('recUser').value;
    // TODO: fetch your real recommendations:
    // const res = await fetch(`/api/recommend?user=${user}&k=5`);
    // const recs = await res.json();
    const recs = ['alice','bob','carol']; // placeholder

    const container = document.getElementById('recResults');
    container.innerHTML = '';
    recs.forEach(name => {
        const div = document.createElement('div');
        div.className = 'col-md-4 rec-card';
        div.innerText = name;
        container.appendChild(div);
    });
});

// Handle file uploads (you’ll wire these to your back-end too)
document.getElementById('friendsForm').addEventListener('submit', async e => {
    e.preventDefault();
    const file = document.getElementById('friendsFile').files[0];
    // TODO: upload via fetch POST /api/upload-friends
    alert(`Would upload CSV: ${file.name}`);
});
document.getElementById('resumeForm').addEventListener('submit', async e => {
    e.preventDefault();
    const file = document.getElementById('resumeFile').files[0];
    // TODO: upload via fetch POST /api/upload-resume
    alert(`Would upload PDF: ${file.name}`);
});

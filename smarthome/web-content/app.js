const API_URL = '/api/stats';

async function fetchStats() {
    try {
        const response = await fetch(API_URL);
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const data = await response.json();
        updateDashboard(data);
        updateStatus(true);
    } catch (error) {
        console.error('Error fetching stats:', error);
        updateStatus(false);
    }
}

function updateStatus(connected) {
    const statusEl = document.getElementById('connection-status');
    if (connected) {
        statusEl.textContent = 'Live Connected';
        statusEl.classList.add('connected');
    } else {
        statusEl.textContent = 'Disconnected';
        statusEl.classList.remove('connected');
    }
}

function updateDashboard(data) {
    // Update Total Energy
    const totalEnergyEl = document.getElementById('total-energy-value');
    // Format to 2 decimal places
    totalEnergyEl.textContent = `${parseFloat(data.totalEnergy).toFixed(2)} W`;

    // Update Rooms
    const roomsContainer = document.getElementById('rooms-container');
    roomsContainer.innerHTML = ''; // Clear current

    data.rooms.forEach(room => {
        const roomCard = document.createElement('div');
        roomCard.className = 'room-card';

        const roomHeader = document.createElement('div');
        roomHeader.className = 'room-header';
        roomHeader.innerHTML = `<div class="room-name">${room.name}</div>`;

        // Calculate room total
        const roomTotal = room.devices.reduce((sum, d) => sum + d.energy, 0);
        const roomTotalEl = document.createElement('div');
        roomTotalEl.style.color = 'var(--text-secondary)';
        roomTotalEl.textContent = `${roomTotal.toFixed(1)} W`;
        roomHeader.appendChild(roomTotalEl);

        roomCard.appendChild(roomHeader);

        const deviceList = document.createElement('div');
        deviceList.className = 'device-list';

        room.devices.forEach((device) => {
            const deviceItem = document.createElement("div");
            deviceItem.className = "device-item";

            // Determine status style
            const isOn = device.status.toUpperCase().includes("ON");
            const statusClass = isOn ? "status-on" : "status-off";

            deviceItem.innerHTML = `
                <div class="device-info">
                    <span class="device-name">${device.name}</span>
                    <span class="device-status ${statusClass}">${device.status}</span>
                </div>
                <div class="device-controls">
                    <span class="device-energy">${device.energy.toFixed(1)} W</span>
                    <button class="toggle-btn ${isOn ? "btn-on" : "btn-off"}" 
                        onclick="toggleDevice('${device.id}')">
                        ${isOn ? "ON" : "OFF"}
                    </button>
                </div>
            `;
            deviceList.appendChild(deviceItem);
        });

        roomCard.appendChild(deviceList);
        roomsContainer.appendChild(roomCard);
    });
}

async function toggleDevice(id) {
    try {
        const response = await fetch(`/api/control?id=${id}&action=toggle`, { method: 'POST' });
        const data = await response.json();
        if (data.status === 'ok') {
            console.log('Toggled device', id);
            fetchStats(); // Refresh immediately
        } else {
            console.error('Failed to toggle:', data.error);
        }
    } catch (e) {
        console.error('Error toggling device:', e);
    }
}

async function handleAddRoom() {
    const nameInput = document.getElementById('new-room-name');
    const name = nameInput.value.trim();
    if (!name) return alert('Please enter a room name');

    try {
        const response = await fetch(`/api/rooms/add?name=${encodeURIComponent(name)}`, { method: 'POST' });
        const data = await response.json();
        if (data.status === 'ok') {
            nameInput.value = ''; // Clear input
            fetchStats();
        } else {
            alert('Error adding room: ' + data.error);
        }
    } catch (e) {
        console.error('Error adding room:', e);
    }
}

async function handleAddDevice() {
    const roomSelect = document.getElementById('device-room-select');
    const nameInput = document.getElementById('new-device-name');
    const typeSelect = document.getElementById('new-device-type');

    const room = roomSelect.value;
    const name = nameInput.value.trim();
    const type = typeSelect.value;

    if (!room) return alert('Please select a room');
    if (!name) return alert('Please enter a device name');

    try {
        const params = new URLSearchParams({ room, name, type });
        const response = await fetch(`/api/devices/add?${params.toString()}`, { method: 'POST' });
        const data = await response.json();
        if (data.status === 'ok') {
            nameInput.value = ''; // Clear input
            fetchStats();
        } else {
            alert('Error adding device: ' + data.error);
        }
    } catch (e) {
        console.error('Error adding device:', e);
    }
}

function updateRoomSelect(rooms) {
    const select = document.getElementById('device-room-select');
    const currentSelection = select.value;

    // Check if we need to update (simple check: count mismatch or name mismatch)
    // For simplicity, we just rebuild if the number of options (minus placeholder) differs 
    // or if we want to be safe, just clear and rebuild if specific logic.
    // Let's iterate and see if names match.

    const existingOptions = Array.from(select.options).map(o => o.value).filter(v => v);
    const newRoomNames = rooms.map(r => r.name);

    const isSame = existingOptions.length === newRoomNames.length &&
        existingOptions.every((val, index) => val === newRoomNames[index]);

    if (isSame) return; // No change needed

    // Rebuild
    select.innerHTML = '<option value="" disabled selected>Select a room...</option>';
    rooms.forEach(room => {
        const option = document.createElement('option');
        option.value = room.name;
        option.textContent = room.name;
        select.appendChild(option);
    });

    // Restore selection if possible
    if (newRoomNames.includes(currentSelection)) {
        select.value = currentSelection;
    }
}

// Update updateDashboard to call updateRoomSelect
const originalUpdateDashboard = updateDashboard;
updateDashboard = function (data) {
    originalUpdateDashboard(data);
    updateRoomSelect(data.rooms);
};

// Initial fetch
fetchStats();

// Poll every 2 seconds
setInterval(fetchStats, 2000);

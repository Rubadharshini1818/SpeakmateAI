$file = 'D:\SpeakmateAI\profile.html'
$content = Get-Content $file -Raw -Encoding UTF8

# --- Replace the entire <script>...</script> block with new working script ---
$newScript = @'
<script>
    const token = localStorage.getItem('speakmate_token');
    if (!token) window.location.href = 'login.html';

    async function loadProfile() {
        try {
            const res = await fetch('/api/profile', { headers: { 'Authorization': 'Bearer ' + token } });
            if (res.status === 401 || res.status === 403) { localStorage.removeItem('speakmate_token'); window.location.href = 'login.html'; return; }
            const data = await res.json();
            window._profileData = data;

            // Hero card
            document.getElementById('profile-display-name').textContent = data.fullName || data.username || '';
            document.getElementById('profile-username').textContent = '@' + (data.username || '').toLowerCase();
            document.getElementById('profile-joined').textContent = 'Member since ' + (data.memberSince || '2024');
            document.getElementById('profile-level').textContent = (data.learningLevel || '') + ' Learner';
            document.getElementById('profile-streak').textContent = (data.currentStreak || 0) + ' Day Streak';
            document.getElementById('profile-xp').textContent = (data.xp || 0) + ' XP';

            // Profile picture
            if (data.profilePictureUrl) {
                document.getElementById('profile-avatar').src = data.profilePictureUrl;
                const hdr = document.getElementById('header-avatar');
                if (hdr) hdr.src = data.profilePictureUrl;
            }

            // Personal info
            document.getElementById('info-fullname').textContent = data.fullName || '—';
            document.getElementById('info-email').textContent = data.email || '—';
            document.getElementById('info-phone').textContent = data.phone || '—';
            document.getElementById('info-dob').textContent = '—';
            document.getElementById('info-gender').textContent = '—';
            document.getElementById('info-location').textContent = data.address || '—';
            document.getElementById('info-native').textContent = '—';
            document.getElementById('info-english-level').textContent = data.learningLevel || '—';

            // Account Activity stats
            const s = document.querySelectorAll('#account-activity-grid .stat-value');
            if (s[0]) s[0].textContent = data.currentStreak || 0;
            if (s[1]) s[1].textContent = Math.round((data.studyMinutes || 0) / 60 * 10) / 10;
            if (s[2]) s[2].textContent = data.completedLessons || 0;
            if (s[3]) s[3].textContent = data.completedInterviews || 0;

        } catch (e) { console.error('Profile load error:', e); }
    }

    // Avatar upload
    function handleAvatarChange(input) {
        if (!input.files || !input.files[0]) return;
        const file = input.files[0];
        if (file.size > 5 * 1024 * 1024) { alert('Image must be under 5 MB.'); return; }
        const reader = new FileReader();
        reader.onload = async function(e) {
            const dataUrl = e.target.result;
            document.getElementById('profile-avatar').src = dataUrl;
            const hdr = document.getElementById('header-avatar');
            if (hdr) hdr.src = dataUrl;
            try {
                await fetch('/api/profile', {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
                    body: JSON.stringify({ profilePictureUrl: dataUrl })
                });
            } catch(err) { console.error(err); }
        };
        reader.readAsDataURL(file);
    }

    // Edit modal
    function openEditProfile() {
        const d = window._profileData || {};
        document.getElementById('edit-fullname').value  = d.fullName  || '';
        document.getElementById('edit-username').value  = d.username  || '';
        document.getElementById('edit-phone').value     = d.phone     || '';
        document.getElementById('edit-address').value   = d.address   || '';
        document.getElementById('edit-level').value     = d.learningLevel || 'B1';
        document.getElementById('edit-error').classList.add('hidden');
        document.getElementById('edit-modal').classList.remove('hidden');
    }

    function closeEditModal() { document.getElementById('edit-modal').classList.add('hidden'); }

    async function saveProfile() {
        const btn = document.getElementById('save-btn');
        const errEl = document.getElementById('edit-error');
        errEl.classList.add('hidden');
        btn.textContent = 'Saving...';
        btn.disabled = true;
        const payload = {
            fullName:      document.getElementById('edit-fullname').value.trim(),
            username:      document.getElementById('edit-username').value.trim(),
            phone:         document.getElementById('edit-phone').value.trim(),
            address:       document.getElementById('edit-address').value.trim(),
            learningLevel: document.getElementById('edit-level').value
        };
        try {
            const res = await fetch('/api/profile', {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
                body: JSON.stringify(payload)
            });
            const data = await res.json();
            if (!res.ok) { errEl.textContent = data.message || 'Update failed.'; errEl.classList.remove('hidden'); }
            else { closeEditModal(); await loadProfile(); }
        } catch(e) { errEl.textContent = 'Network error.'; errEl.classList.remove('hidden'); }
        finally { btn.textContent = 'Save Changes'; btn.disabled = false; }
    }

    document.getElementById('edit-modal').addEventListener('click', function(e) { if (e.target === this) closeEditModal(); });

    // Logout
    document.querySelectorAll('section.bg-red-50 a').forEach((a, i) => {
        if (i === 0) a.onclick = (e) => { e.preventDefault(); localStorage.removeItem('speakmate_token'); window.location.href = 'login.html'; };
    });

    // Back button
    const backBtn = document.querySelector('main button');
    if (backBtn) backBtn.onclick = () => window.location.href = 'dashboard.html';

    document.addEventListener('DOMContentLoaded', loadProfile);
</script>
'@

$editModal = @'

<!-- Edit Profile Modal -->
<div id="edit-modal" class="hidden fixed inset-0 bg-black/50 z-[100] flex items-center justify-center p-4">
  <div class="bg-white rounded-2xl shadow-2xl w-full max-w-[520px] max-h-[90vh] overflow-y-auto">
    <div class="flex items-center justify-between p-6 border-b border-surface-variant">
      <h2 class="font-headline-md text-headline-md text-primary">Edit Profile</h2>
      <button onclick="closeEditModal()" class="w-9 h-9 rounded-full hover:bg-surface-container flex items-center justify-center transition-colors">
        <span class="material-symbols-outlined text-on-surface-variant">close</span>
      </button>
    </div>
    <div class="p-6 space-y-4">
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div class="space-y-1">
          <label class="text-label-sm text-on-surface-variant font-bold">Full Name</label>
          <input id="edit-fullname" type="text" class="w-full border border-outline-variant rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-primary focus:outline-none" placeholder="Your full name"/>
        </div>
        <div class="space-y-1">
          <label class="text-label-sm text-on-surface-variant font-bold">Username</label>
          <input id="edit-username" type="text" class="w-full border border-outline-variant rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-primary focus:outline-none" placeholder="username"/>
        </div>
        <div class="space-y-1">
          <label class="text-label-sm text-on-surface-variant font-bold">Phone</label>
          <input id="edit-phone" type="tel" class="w-full border border-outline-variant rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-primary focus:outline-none" placeholder="+91 98765 43210"/>
        </div>
        <div class="space-y-1">
          <label class="text-label-sm text-on-surface-variant font-bold">English Level</label>
          <select id="edit-level" class="w-full border border-outline-variant rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-primary focus:outline-none bg-white">
            <option value="A1">A1 — Beginner</option>
            <option value="A2">A2 — Elementary</option>
            <option value="B1">B1 — Intermediate</option>
            <option value="B2">B2 — Upper Intermediate</option>
            <option value="C1">C1 — Advanced</option>
            <option value="C2">C2 — Proficient</option>
          </select>
        </div>
        <div class="space-y-1 sm:col-span-2">
          <label class="text-label-sm text-on-surface-variant font-bold">Address / Location</label>
          <input id="edit-address" type="text" class="w-full border border-outline-variant rounded-lg px-4 py-2.5 text-sm focus:ring-2 focus:ring-primary focus:outline-none" placeholder="City, Country"/>
        </div>
      </div>
      <p id="edit-error" class="text-error text-sm hidden"></p>
    </div>
    <div class="flex gap-3 p-6 border-t border-surface-variant">
      <button onclick="closeEditModal()" class="flex-1 py-2.5 rounded-full border border-outline-variant font-bold text-on-surface-variant hover:bg-surface-container transition-colors text-sm">Cancel</button>
      <button onclick="saveProfile()" id="save-btn" class="flex-1 py-2.5 rounded-full font-bold text-sm transition-all active:scale-95" style="background:linear-gradient(135deg,#F5C84C,#F2B544);color:#241a00;">Save Changes</button>
    </div>
  </div>
</div>
'@

# Replace old script block with new one
$content = $content -replace '(?s)<script>.*?</script>', $newScript

# Add edit modal before </body>
$content = $content -replace '</body>', ($editModal + "`r`n</body>")

# Fix the Account Activity grid — add id and stat-value classes so JS can target them
$content = $content -replace '<div class="grid grid-cols-2 gap-4">', '<div class="grid grid-cols-2 gap-4" id="account-activity-grid">'
$content = $content -replace '(?s)id="account-activity-grid">(\r?\n)(.*?Login Days)', {
    $m = $args[0]
    $m.Value -replace '<span class="text-display-lg text-primary text-\[32px\] leading-tight">142</span>', '<span class="text-display-lg text-primary text-[32px] leading-tight stat-value" id="stat-streak">0</span>'
}

# Also fix Certificates & Account Activity wrapper — change to single column since certificates removed
$content = $content -replace '<!-- Certificates & Account Activity -->\r?\n<div class="grid grid-cols-1 md:grid-cols-2 gap-gutter">', '<!-- Account Activity -->' + "`r`n" + '<div class="grid grid-cols-1 gap-gutter">'

Set-Content $file -Value $content -Encoding UTF8 -NoNewline
Write-Output "All done"

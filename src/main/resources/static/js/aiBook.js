let aiWaiting = false;
let isComposingQuestion = false;
let ignoreEnterUntil = 0;

window.onload = function() {
  const role = sessionStorage.getItem('role');
  const username = sessionStorage.getItem('username');

  if (!role) {
    window.location.href = 'login.html';
    return;
  }

  document.getElementById('header-username').textContent = username || '';
  document.getElementById('dropdown-username').textContent = username || '';

  const adminIcon = `<i id="user-icon" class="fa-solid fa-user-shield" style="font-size:15px;color:#F5C9C9;"></i>`;
  const userIcon = `<i id="user-icon" class="fa-solid fa-user" style="font-size:15px;color:#F5C9C9;"></i>`;
  document.getElementById('user-icon').outerHTML = role === 'ADMIN' ? adminIcon : userIcon;

  appendMessage('assistant', '您好，我是書庫智能客服。您可以詢問書名、作者、出版社、價格或推薦方向，我會根據目前書庫資料回答。');
  bindQuestionInput();
};

function bindQuestionInput() {
  const questionInput = document.getElementById('ai-question');

  questionInput.addEventListener('compositionstart', function() {
    isComposingQuestion = true;
  });

  questionInput.addEventListener('compositionend', function() {
    isComposingQuestion = false;
    ignoreEnterUntil = Date.now() + 80;
  });

  questionInput.addEventListener('input', function() {
    resizeQuestionInput(this);
  });

  questionInput.addEventListener('keydown', function(e) {
    if (isImeEnter(e)) {
      if (!isComposingQuestion && !e.isComposing) {
        e.preventDefault();
      }
      return;
    }

    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      document.querySelector('.ai-input-bar').requestSubmit();
    }
  });
}

async function handleAiSubmit(event) {
  event.preventDefault();

  if (aiWaiting) return;

  const input = document.getElementById('ai-question');
  const question = input.value.trim();

  if (!question) {
    input.focus();
    return;
  }

  appendMessage('user', question);
  clearQuestionInput(input);

  setAiWaiting(true);
  const loadingBubble = appendLoadingMessage();

  try {
    const response = await aiBookService.ask(question);
    loadingBubble.remove();
    appendMessage('assistant', response.answer || '目前沒有取得回答，請稍後再試。');
  } catch (error) {
    loadingBubble.remove();
    if (error.message && error.message.includes('401')) {
      sessionStorage.clear();
      window.location.href = 'login.html';
      return;
    }
    appendMessage('assistant', 'AI 助理暫時無法回覆，請確認後端服務與 Ollama 模型是否已啟動。');
  } finally {
    setAiWaiting(false);
    input.focus();
  }
}

function useSuggestion(question) {
  const input = document.getElementById('ai-question');
  input.value = question;
  resizeQuestionInput(input);
  input.focus();
  document.querySelector('.ai-input-bar').requestSubmit();
}

function isImeEnter(event) {
  return event.key === 'Enter'
      && (isComposingQuestion
          || event.isComposing
          || event.keyCode === 229
          || Date.now() < ignoreEnterUntil);
}

function clearQuestionInput(input) {
  input.value = '';
  resizeQuestionInput(input);
}

function resizeQuestionInput(input) {
  input.style.height = 'auto';
  input.style.height = input.value ? `${Math.min(input.scrollHeight, 120)}px` : '44px';
}

function appendMessage(sender, text) {
  const chat = document.getElementById('ai-chat');
  const row = document.createElement('div');
  row.className = `ai-message-row ${sender}`;

  const avatar = document.createElement('div');
  avatar.className = 'ai-avatar';
  avatar.innerHTML = sender === 'user'
      ? '<i class="fa-solid fa-user"></i>'
      : '<i class="fa-solid fa-headset"></i>';

  const bubble = document.createElement('div');
  bubble.className = 'ai-bubble';
  bubble.textContent = text;

  row.appendChild(avatar);
  row.appendChild(bubble);
  chat.appendChild(row);
  scrollChatToBottom();

  return row;
}

function appendLoadingMessage() {
  const chat = document.getElementById('ai-chat');
  const row = document.createElement('div');
  row.className = 'ai-message-row assistant';

  const avatar = document.createElement('div');
  avatar.className = 'ai-avatar';
  avatar.innerHTML = '<i class="fa-solid fa-headset"></i>';

  const bubble = document.createElement('div');
  bubble.className = 'ai-bubble ai-loading';
  bubble.innerHTML = '<span></span><span></span><span></span>';

  row.appendChild(avatar);
  row.appendChild(bubble);
  chat.appendChild(row);
  scrollChatToBottom();

  return row;
}

function setAiWaiting(waiting) {
  aiWaiting = waiting;
  document.getElementById('ai-send-btn').disabled = waiting;
  document.querySelectorAll('.ai-suggestions button').forEach(function(button) {
    button.disabled = waiting;
  });
}

function scrollChatToBottom() {
  const chat = document.getElementById('ai-chat');
  chat.scrollTop = chat.scrollHeight;
}

function goBackToBooks() {
  window.location.href = 'index.html';
}

function toggleDropdown() {
  const menu = document.getElementById('dropdown-menu');
  const toggle = document.getElementById('user-toggle');
  menu.classList.toggle('show');
  toggle.classList.toggle('open');
}

document.addEventListener('click', function(e) {
  const dropdown = document.querySelector('.user-dropdown');
  if (dropdown && !dropdown.contains(e.target)) {
    document.getElementById('dropdown-menu').classList.remove('show');
    document.getElementById('user-toggle').classList.remove('open');
  }
});

async function handleLogout() {
  try {
    await authService.logout();
  } catch (error) {
    console.error('Logout error:', error);
  } finally {
    sessionStorage.clear();
    window.location.href = 'login.html';
  }
}

import React, { useState, useRef, useEffect } from 'react';
import { Send, Bot, User, Sparkles, Loader2 } from 'lucide-react';
import { sendChatMessage } from '../api';

export default function AiCoachChatView({ profile }) {
  const [messages, setMessages] = useState([
    {
      role: 'assistant',
      content: `Hello ${profile?.fullName || 'there'}! I am your **AI Career Coach** powered by Google Gemini. 🚀\n\nI am here to assist you with:\n- 🌏 **Global Career Transition & Job Search** (Singapore, Europe, Japan, US, Worldwide Remote).\n- 🛂 **Visa Sponsorship & Eligibility Insights** for software and tech engineers.\n- 📝 **ATS Resume Tailoring** and rewriting achievements using the STAR methodology.\n- 🎤 **Mock Interviews** (Behavioral scenarios & System Design architecture).\n\nWhat career goal or question would you like to explore today?`,
    },
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (messages.length <= 1) {
      setMessages([
        {
          role: 'assistant',
          content: `Hello ${profile?.fullName || 'there'}! I am your **AI Career Coach** powered by Google Gemini 3.5 Flash. 🚀\n\nI am currently analyzing your profile as **${profile?.currentTitle || 'Tech Professional'}** (${profile?.yearsOfExperience || 0} years of experience, core stack: ${(profile?.skills || []).slice(0, 5).join(', ')}).\n\nI am here to assist you with:\n- 🌏 **Global Career Transition & Job Search** (Singapore, Europe, Japan, US, Worldwide Remote).\n- 🛂 **Visa Sponsorship & Eligibility Insights** for software and tech engineers.\n- 📝 **ATS Resume Tailoring** and rewriting achievements using the STAR methodology.\n- 🎤 **Mock Interviews** (Behavioral scenarios & System Design architecture).\n\nWhat career goal or question would you like to explore today?`,
        },
      ]);
    }
  }, [profile?.fullName, profile?.currentTitle, profile?.updatedAt]);

  useEffect(() => {
    scrollToBottom();
  }, [messages, loading]);

  const handleSend = async (userText = null) => {
    const textToSend = typeof userText === 'string' ? userText : input;
    if (!textToSend.trim() || loading) return;

    const userMessage = { role: 'user', content: textToSend.trim() };
    const updatedMessages = [...messages, userMessage];
    setMessages(updatedMessages);
    setInput('');
    setLoading(true);

    try {
      const history = messages.slice(-6); // last 6 messages for context
      const res = await sendChatMessage(textToSend.trim(), history);
      setMessages([...updatedMessages, { role: 'assistant', content: res.reply }]);
    } catch (err) {
      setMessages([
        ...updatedMessages,
        { role: 'assistant', content: 'Apologies, an error occurred while connecting to the AI Coach. Please try again!' },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const quickPrompts = [
    '✈️ Visa sponsorship requirements for Software Engineers in Singapore?',
    '🇩🇪 How do I qualify for an EU Blue Card in Germany as a Developer?',
    '🎤 Ask me 3 high-frequency System Design interview questions',
    '📝 Rewrite this bullet point to highlight metrics using the STAR method',
    '💰 How to negotiate compensation with international tech employers?',
  ];

  // Markdown formatter
  const renderFormattedContent = (content) => {
    const lines = content.split('\n');
    return lines.map((line, idx) => {
      let processed = line;
      if (line.startsWith('### ')) {
        return <h3 key={idx} style={{ color: '#fff', margin: '0.6rem 0 0.3rem', fontSize: '1.05rem' }}>{line.replace('### ', '')}</h3>;
      }
      if (line.startsWith('## ')) {
        return <h2 key={idx} style={{ color: '#fff', margin: '0.8rem 0 0.4rem', fontSize: '1.2rem' }}>{line.replace('## ', '')}</h2>;
      }
      if (line.startsWith('- ') || line.startsWith('* ')) {
        return (
          <div key={idx} style={{ display: 'flex', gap: '0.5rem', marginLeft: '0.5rem', marginBottom: '0.2rem' }}>
            <span style={{ color: 'var(--accent-primary)' }}>•</span>
            <span dangerouslySetInnerHTML={{ __html: formatBold(line.substring(2)) }} />
          </div>
        );
      }
      if (line.trim() === '') {
        return <div key={idx} style={{ height: '0.4rem' }} />;
      }
      return (
        <p key={idx} style={{ marginBottom: '0.35rem' }} dangerouslySetInnerHTML={{ __html: formatBold(processed) }} />
      );
    });
  };

  const formatBold = (text) => {
    return text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/`([^`]+)`/g, '<code style="background:rgba(255,255,255,0.1);padding:2px 5px;border-radius:4px;font-family:monospace">$1</code>');
  };

  return (
    <div>
      <div className="page-header">
        <div className="page-header-text">
          <h1>Live AI Career Coach & Advisory</h1>
          <p>
            Engage with an AI career strategist powered by Google Gemini to practice technical interviews, negotiate offers, and navigate global visa pathways.
          </p>
        </div>
      </div>

      <div className="chat-wrapper">
        <div className="chat-messages">
          {messages.map((msg, idx) => (
            <div
              key={idx}
              className={`chat-bubble ${msg.role === 'user' ? 'chat-bubble-user' : 'chat-bubble-ai'}`}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.4rem', fontSize: '0.75rem', opacity: 0.8 }}>
                {msg.role === 'user' ? (
                  <>
                    <User size={13} />
                    <span>You</span>
                  </>
                ) : (
                  <>
                    <Bot size={13} color="#818cf8" />
                    <span style={{ color: '#818cf8', fontWeight: '700' }}>AI Career Coach (Gemini)</span>
                  </>
                )}
              </div>
              <div style={{ lineHeight: '1.55' }}>
                {renderFormattedContent(msg.content)}
              </div>
            </div>
          ))}

          {loading && (
            <div className="chat-bubble chat-bubble-ai" style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
              <Loader2 className="animate-spin" size={16} color="#818cf8" />
              <span style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>AI Career Coach is analyzing and thinking...</span>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        {/* Quick Prompts */}
        <div className="chat-suggestions">
          {quickPrompts.map((prompt, idx) => (
            <button
              key={idx}
              className="suggestion-chip"
              onClick={() => handleSend(prompt)}
              disabled={loading}
            >
              {prompt}
            </button>
          ))}
        </div>

        {/* Input Row */}
        <div className="chat-input-row">
          <input
            type="text"
            className="chat-input"
            placeholder="Ask AI Coach about career strategies, system design interviews, global visa pathways..."
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={loading}
          />
          <button
            className="btn btn-primary"
            onClick={() => handleSend()}
            disabled={loading || !input.trim()}
          >
            {loading ? <Loader2 className="animate-spin" size={16} /> : <Send size={16} />}
            Send
          </button>
        </div>
      </div>
    </div>
  );
}

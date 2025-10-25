/* ==================== ERINIUM FACTION - COMMON I18N & THEMES JS ==================== */
/* Système de thèmes et traductions réutilisable pour toutes les pages */

(function() {
    'use strict';

    // ==================== THEME MANAGEMENT ====================
    const ThemeManager = {
        init: function() {
            const root = document.documentElement;
            const savedTheme = localStorage.getItem('ef-theme') || 'erinium';

            // Apply saved theme
            root.setAttribute('data-theme', savedTheme);

            // Set active button
            const themeButtons = document.querySelectorAll('.ef-theme-btn');
            themeButtons.forEach(btn => {
                btn.classList.toggle('active', btn.dataset.theme === savedTheme);

                // Add click event
                btn.addEventListener('click', () => {
                    const theme = btn.dataset.theme;
                    root.setAttribute('data-theme', theme);
                    localStorage.setItem('ef-theme', theme);

                    themeButtons.forEach(b => b.classList.remove('active'));
                    btn.classList.add('active');
                });
            });
        }
    };

    // ==================== LANGUAGE MANAGEMENT ====================
    const LanguageManager = {
        currentLang: 'fr',
        translations: {},

        init: function(translations) {
            this.translations = translations;
            const savedLang = localStorage.getItem('ef-lang') || 'fr';
            this.currentLang = savedLang;

            // Apply saved language
            document.documentElement.lang = savedLang;
            this.applyTranslations(savedLang);

            // Set active button
            const langButtons = document.querySelectorAll('.ef-lang-btn');
            langButtons.forEach(btn => {
                btn.classList.toggle('active', btn.dataset.lang === savedLang);

                // Add click event
                btn.addEventListener('click', () => {
                    const lang = btn.dataset.lang;
                    this.currentLang = lang;
                    document.documentElement.lang = lang;
                    localStorage.setItem('ef-lang', lang);

                    langButtons.forEach(b => b.classList.remove('active'));
                    btn.classList.add('active');

                    this.applyTranslations(lang);
                });
            });
        },

        applyTranslations: function(lang) {
            const elements = document.querySelectorAll('[data-i18n]');
            elements.forEach(element => {
                const key = element.dataset.i18n;
                if (this.translations[lang] && this.translations[lang][key]) {
                    // Preserve HTML if needed
                    if (element.dataset.i18nHtml === 'true') {
                        element.innerHTML = this.translations[lang][key];
                    } else {
                        element.textContent = this.translations[lang][key];
                    }
                }
            });
        },

        t: function(key) {
            return this.translations[this.currentLang]?.[key] || key;
        }
    };

    // ==================== CONTROLS BAR INJECTION ====================
    const ControlsBar = {
        inject: function() {
            const controlsHTML = `
                <div class="ef-controls-bar">
                    <div class="ef-controls-left">
                        <a href="../Erinium_Faction_Portfolio.html" class="ef-home-btn">
                            ← <span data-i18n="home">Accueil</span>
                        </a>
                    </div>
                    <div class="ef-controls-right">
                        <div class="ef-control-group">
                            <span class="ef-control-label" data-i18n="theme">Thème</span>
                            <button class="ef-theme-btn active" data-theme="erinium">Erinium</button>
                            <button class="ef-theme-btn" data-theme="dark">Dark</button>
                            <button class="ef-theme-btn" data-theme="light">Light</button>
                        </div>
                        <div class="ef-control-group">
                            <span class="ef-control-label" data-i18n="language">Langue</span>
                            <button class="ef-lang-btn active" data-lang="fr">🇫🇷 FR</button>
                            <button class="ef-lang-btn" data-lang="en">🇬🇧 EN</button>
                        </div>
                    </div>
                </div>
            `;

            // Insert at the beginning of body
            document.body.insertAdjacentHTML('afterbegin', controlsHTML);

            // Wrap existing content
            const existingContent = Array.from(document.body.children).slice(1); // Skip controls bar
            const wrapper = document.createElement('div');
            wrapper.className = 'ef-content-wrapper';
            existingContent.forEach(child => wrapper.appendChild(child));
            document.body.appendChild(wrapper);
        }
    };

    // ==================== COMMON TRANSLATIONS ====================
    const commonTranslations = {
        fr: {
            home: 'Accueil',
            theme: 'Thème',
            language: 'Langue'
        },
        en: {
            home: 'Home',
            theme: 'Theme',
            language: 'Language'
        }
    };

    // ==================== INITIALIZATION ====================
    window.EriniumFaction = {
        ThemeManager: ThemeManager,
        LanguageManager: LanguageManager,
        ControlsBar: ControlsBar,
        commonTranslations: commonTranslations,

        init: function(pageTranslations) {
            // Inject controls bar
            ControlsBar.inject();

            // Merge common translations with page-specific ones
            const mergedTranslations = {
                fr: { ...commonTranslations.fr, ...pageTranslations.fr },
                en: { ...commonTranslations.en, ...pageTranslations.en }
            };

            // Initialize managers
            ThemeManager.init();
            LanguageManager.init(mergedTranslations);
        }
    };

    // Auto-initialize on DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            // Wait for page-specific initialization
        });
    }
})();

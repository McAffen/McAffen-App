package de.mcaffen.app.ui

/**
 * Englische Übersetzung der Rechtstexte (nur zum besseren Verständnis).
 * Maßgeblich ist die deutsche Fassung in `Legal.kt`.
 */
val impressumDocumentEn = LegalDocument(
    title = "Imprint",
    sourceUrl = "https://mcaffen.de/#impressum",
    sections = listOf(
        LegalSection(
            null,
            text(
                "Information pursuant to Section 5 DDG. Responsible for the website and the related online services of the Minecraft server McAffen.de.",
                "Responsible:\nMarcel Franz",
                "Address:\nSophienstraße 16\n24768 Rendsburg\nGermany",
                "Email:\nmarcelfranz662@gmail.com",
                "Phone:\n0162/3905746",
                "Responsible for content:\nMarcel Franz",
            ),
        ),
    ),
)

val datenschutzDocumentEn = LegalDocument(
    title = "Privacy policy",
    sourceUrl = "https://mcaffen.de/#datenschutz",
    sections = listOf(
        LegalSection("1. Controller", text("Marcel Franz\nSophienstraße 16\n24768 Rendsburg, Germany\nEmail: marcelfranz662@gmail.com")),
        LegalSection("2. Scope", text("This privacy policy applies to the website mcaffen.de and the related services of the McAffen network.")),
        LegalSection("3. Hosting", text("Our website is hosted by ProHosting24. Technical data required for operating, securing and delivering the website may be processed in the process.")),
        LegalSection("4. Cloudflare", text("We use Cloudflare for DNS, proxy services and technical protection features. In particular, IP addresses and connection data may be processed.")),
        LegalSection("5. Server log data", text("When you visit the website, data such as your IP address, the time of access, the requested page, browser information and operating system may be processed. This data serves technical operation and security.")),
        LegalSection("6. Cookies", text("The website uses the session cookie PHPSESSID for functions such as login, the admin area and tickets. It is used to assign a session.")),
        LegalSection("7. Local browser storage", text("Settings such as language, effects, the Minecraft name on the vote page, the ticket overview and the cookie choice may be stored in localStorage.")),
        LegalSection("8. Cookie choice", text("The cookie banner offers the options “Accept” and “Necessary only”. The choice is stored in the browser. Technically necessary functions may still be used. The actual effect of the choice depends on which services are actually loaded.")),
        LegalSection("9. Minecraft heads", text("On certain pages, Minecraft heads are loaded from mc-heads.net. Your IP address may be transmitted to this third-party provider. Whether this connection is prevented with “Necessary only” depends on the technical implementation.")),
        LegalSection("10. Fonts and own resources", text("To the best of our current knowledge, fonts and the other locally embedded resources are provided from our own server.")),
        LegalSection("11. External embeds", text("Embedded content from third parties, for example Discord or YouTube, may establish a connection to their servers when loaded. Data such as your IP address may be transmitted. The privacy policies of the respective provider apply to any further processing.")),
        LegalSection("12. External links", text("Our website contains links to external services. When you click a link, the privacy policies of the respective provider apply.")),
        LegalSection("13. Minecraft servers", text("When playing on our Minecraft servers, data such as player name, UUID, IP address, inventories, game statistics, chat messages, commands, votes and economic activity may be processed.")),
        LegalSection("14. Moderation and sanctions", text("To enforce our server rules, we may process information about warnings, mutes, bans, unban requests and reported violations.")),
        LegalSection("15. Public player information", text("Player names, online status and certain game statistics may be displayed publicly on our website or within the network.")),
        LegalSection("16. Tickets and unban requests", text("For support tickets and unban requests, we process the information you submit and the information required to handle the case. The data is deleted manually after the case has been reviewed, unless further retention is required.")),
        LegalSection("17. Discord and account links", text("When using Discord features or linking a Discord account to Minecraft, Discord identifiers and related linking data may be processed.")),
        LegalSection("18. Databases", text("To operate our services, databases including MongoDB and MySQL are used. They may store, for example, player data, settings, statistics and support information.")),
        LegalSection("19. Retention period", text("Data is only stored for as long as it is needed for the respective purpose or as long as statutory or legitimate retention reasons exist. Depending on the type of data and the system, deletion is partly done manually.")),
        LegalSection("20. No advertising or analytics trackers", text("To the best of our current knowledge, we do not use advertising or analytics trackers such as Google Analytics. Technically necessary functions and embedded third-party services are to be distinguished from this.")),
        LegalSection("21. Your rights", text("Within the scope of the statutory requirements, you have the right of access, rectification, erasure, restriction of processing, data portability and objection. You can also lodge a complaint with a competent data protection supervisory authority.")),
        LegalSection("22. Contact and changes", text("If you have questions about data protection, you can reach us at McAffe13@proton.me. We may amend this privacy policy if our website, the services used or the legal requirements change.")),
    ),
)

val agbDocumentEn = LegalDocument(
    title = "Terms and conditions",
    sourceUrl = "https://mcaffen.de/#agb",
    sections = listOf(
        LegalSection(null, text("As of: October 2026")),
        LegalSection("§ 1 – Scope", text("These terms apply to the website McAffen.de, the McAffen Minecraft network and the related community services. By using them, you agree to these terms and the applicable server rules, to the extent permitted by law.")),
        LegalSection("§ 2 – Provider", text("Marcel Franz\nSophienstraße 16, 24768 Rendsburg, Germany\nEmail: marcelfranz662@gmail.com")),
        LegalSection("§ 3 – Use and server rules", text("All users must follow the server rules and treat other players with respect. In particular, cheats, unauthorized mods, bug abuse, item duplication, griefing, real-money trading of items, coins or accounts, spam, insults, unauthorized advertising and circumventing bans are prohibited.\n\nThe complete rules and the allowed and prohibited modifications can be found at mcaffen.de/regeln.")),
        LegalSection("§ 4 – Measures for rule violations", text("In case of violations, the team may, depending on severity and frequency, issue warnings, mutes, temporary or permanent bans and other appropriate measures.\n\nThe published penalty catalog serves as a guideline. For particularly serious violations, the team may deviate from it. A review can be requested via the ticket system or an unban request.")),
        LegalSection("§ 5 – Game progress and virtual values", text("Game progress, inventories, statistics and virtual currencies are part of the game offering. Technical errors, maintenance work or other disruptions may lead to data loss or changes. There is no entitlement to permanent availability or to unchanged retention, to the extent permitted by law.\n\nVirtual currencies and items cannot be paid out in real money. Trading items, coins or accounts for real money is prohibited.")),
        LegalSection("§ 6 – Availability and changes", text("McAffen may carry out maintenance work, change features and adjust or discontinue game modes. Constant availability is not guaranteed. Statutory claims remain unaffected.")),
        LegalSection("§ 7 – Liability", text("McAffen is liable in accordance with the statutory provisions, in particular in cases of intent, gross negligence and injury to life, body or health. In the case of slightly negligent breaches of duty, the statutory limitations of liability apply, to the extent permitted.")),
        LegalSection("§ 8 – Data protection", text("Information on the processing of personal data can be found in our privacy policy.")),
        LegalSection("§ 9 – Final provisions", text("German law applies, unless mandatory statutory provisions, in particular consumer protection rights, state otherwise. Should individual provisions be invalid, the remaining provisions remain unaffected to the extent permitted by law.\n\nContact: marcelfranz662@gmail.com")),
    ),
)

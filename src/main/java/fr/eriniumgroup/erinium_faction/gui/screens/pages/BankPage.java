package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import com.mojang.blaze3d.platform.InputConstants;
import fr.eriniumgroup.erinium_faction.common.network.packets.BankDepositMessage;
import fr.eriniumgroup.erinium_faction.common.network.packets.BankWithdrawMessage;
import fr.eriniumgroup.erinium_faction.common.network.packets.SyncTransactionHistoryMessage;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.gui.screens.FactionClientData;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ScrollList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Page Bank - Gestion de la banque de faction
 * Permet de déposer/retirer de l'argent et voir l'historique des transactions
 */
public class BankPage extends FactionPage {

    // Textures
    private static final ResourceLocation BALANCE_DISPLAY = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/bank/balance-display.png");
    private static final ResourceLocation INPUT_FIELD = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/bank/input-field.png");
    private static final ResourceLocation BUTTON_DEPOSIT_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/bank/button-deposit-normal.png");
    private static final ResourceLocation BUTTON_DEPOSIT_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/bank/button-deposit-hover.png");
    private static final ResourceLocation BUTTON_WITHDRAW_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/bank/button-withdraw-normal.png");
    private static final ResourceLocation BUTTON_WITHDRAW_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/bank/button-withdraw-hover.png");
    private static final ResourceLocation TRANSACTION_ITEM_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/bank/transaction-item-normal.png");
    private static final ResourceLocation TRANSACTION_ITEM_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/bank/transaction-item-hover.png");

    // Composants
    private ScrollList<Transaction> transactionScrollList;
    private StringBuilder amountInput = new StringBuilder();
    private boolean inputFocused = false;
    private int cursorBlink = 0;

    // Boutons positions
    private int depositBtnX, depositBtnY, depositBtnW, depositBtnH;
    private int withdrawBtnX, withdrawBtnY, withdrawBtnW, withdrawBtnH;
    private int inputX, inputY, inputW, inputH;

    // Classe pour les transactions
    private static class Transaction {
        enum Type { DEPOSIT, WITHDRAW }
        Type type;
        String playerName;
        long amount;
        String timestamp;

        Transaction(Type type, String playerName, long amount, String timestamp) {
            this.type = type;
            this.playerName = playerName;
            this.amount = amount;
            this.timestamp = timestamp;
        }
    }

    public BankPage(Font font) {
        super(font);
    }

    private void initComponents(int leftPos, int topPos, double scaleX, double scaleY) {
        if (transactionScrollList == null) {
            // Créer scroll list pour l'historique des transactions
            transactionScrollList = new ScrollList<>(font, this::renderTransactionItem, sh(40, scaleY));
        }

        // Mettre à jour les transactions depuis FactionClientData
        List<Transaction> transactions = new ArrayList<>();
        List<SyncTransactionHistoryMessage.TransactionData> history = FactionClientData.getTransactionHistory();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (SyncTransactionHistoryMessage.TransactionData data : history) {
            Transaction.Type type = data.type.equals("DEPOSIT") ? Transaction.Type.DEPOSIT : Transaction.Type.WITHDRAW;
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(data.timestamp), ZoneId.systemDefault());
            String formattedTime = dateTime.format(formatter);
            transactions.add(new Transaction(type, data.playerName, data.amount, formattedTime));
        }

        transactionScrollList.setItems(transactions);

        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);

        // Scroll list pour transactions (remonté pour mieux utiliser l'espace)
        transactionScrollList.setBounds(x, y + sh(108, scaleY), w, sh(103, scaleY));

        // Positions des boutons
        depositBtnX = x;
        depositBtnY = y + sh(72, scaleY);
        depositBtnW = sw(130, scaleX);
        depositBtnH = sh(26, scaleY);

        withdrawBtnX = x + sw(145, scaleX);
        withdrawBtnY = y + sh(72, scaleY);
        withdrawBtnW = sw(130, scaleX);
        withdrawBtnH = sh(26, scaleY);

        // Position du champ input
        inputX = x + sw(57, scaleX);
        inputY = y + sh(39, scaleY) + 3;
        inputW = sw(160, scaleX);
        inputH = sh(26, scaleY);
    }

    private void renderTransactionItem(GuiGraphics g, Transaction transaction, int x, int y, int width, int height, boolean hovered, Font font, int mouseX, int mouseY) {
        // Utiliser l'image appropriée
        ResourceLocation texture = hovered ? TRANSACTION_ITEM_HOVER : TRANSACTION_ITEM_NORMAL;
        ImageRenderer.renderScaledImage(g, texture, x, y, width, height);

        // Icône du type de transaction
        String typeIcon = transaction.type == Transaction.Type.DEPOSIT ? "+" : "-";
        int typeColor = transaction.type == Transaction.Type.DEPOSIT ? 0xFF10b981 : 0xFFef4444;
        g.drawString(font, typeIcon, x + 8, y + (height / 2) - 4, typeColor, false);

        // Nom du joueur
        g.drawString(font, transaction.playerName, x + 24, y + 6, 0xFFa0a0c0, false);

        // Montant
        String amountText = (transaction.type == Transaction.Type.DEPOSIT ? "+" : "-") + transaction.amount + " coins";
        g.drawString(font, amountText, x + 24, y + 18, typeColor, false);

        // Timestamp (à droite)
        int timestampWidth = font.width(transaction.timestamp);
        g.drawString(font, transaction.timestamp, x + width - timestampWidth - 8, y + (height / 2) - 4, 0xFF6a6a7e, false);
    }

    @Override
    public void render(GuiGraphics g, int leftPos, int topPos, double scaleX, double scaleY, int mouseX, int mouseY) {
        initComponents(leftPos, topPos, scaleX, scaleY);

        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);

        // Récupérer la balance de faction
        var data = getFactionData();
        long balance = data != null ? data.bank : 0;

        // Balance display
        int balanceX = x;
        int balanceY = y;
        int balanceW = w;
        int balanceH = sh(40, scaleY);

        ImageRenderer.renderScaledImage(g, BALANCE_DISPLAY, balanceX, balanceY, balanceW, balanceH);

        // Label "Balance:"
        g.drawString(font, translate("erinium_faction.gui.bank.balance"), balanceX + sw(9, scaleX), balanceY + sh(10, scaleY), 0xFFa0a0c0, false);

        // Montant de la balance
        String balanceText = balance + " coins";
        g.drawString(font, balanceText, balanceX + sw(9, scaleX), balanceY + sh(22, scaleY), 0xFFfbbf24, true);

        // Monnaie personnelle du joueur (à droite)
        double playerBalance = FactionClientData.getPlayerBalance();
        String playerBalanceLabel = translate("erinium_faction.gui.bank.your_balance");
        String playerBalanceText = String.format("%.0f coins", playerBalance);

        int labelWidth = font.width(playerBalanceLabel);
        int textWidth = font.width(playerBalanceText);
        int rightX = balanceX + balanceW - sw(9, scaleX);

        g.drawString(font, playerBalanceLabel, rightX - labelWidth, balanceY + sh(10, scaleY), 0xFFa0a0c0, false);
        g.drawString(font, playerBalanceText, rightX - textWidth, balanceY + sh(22, scaleY), 0xFF10b981, true);

        // Label "Amount:"
        g.drawString(font, translate("erinium_faction.gui.bank.amount"), x, y + sh(34, scaleY) + 10, 0xFFa0a0c0, false);

        // Input field
        ImageRenderer.renderScaledImage(g, INPUT_FIELD, inputX, inputY, inputW, inputH);

        // Texte dans l'input
        String inputText = amountInput.toString();
        if (inputText.isEmpty() && !inputFocused) {
            // Placeholder
            g.drawString(font, "0", inputX + sw(8, scaleX), inputY + sh(9, scaleY), 0xFF6a6a7e, false);
        } else {
            g.drawString(font, inputText, inputX + sw(8, scaleX), inputY + sh(9, scaleY), 0xFFffffff, false);
        }

        // Curseur clignotant si focus
        if (inputFocused && (cursorBlink / 10) % 2 == 0) {
            int cursorX = inputX + sw(8, scaleX) + font.width(inputText);
            g.fill(cursorX, inputY + sh(7, scaleY), cursorX + 1, inputY + sh(19, scaleY), 0xFFffffff);
        }

        // Boutons Deposit et Withdraw
        boolean depositHovered = mouseX >= depositBtnX && mouseX < depositBtnX + depositBtnW &&
                                 mouseY >= depositBtnY && mouseY < depositBtnY + depositBtnH;
        boolean withdrawHovered = mouseX >= withdrawBtnX && mouseX < withdrawBtnX + withdrawBtnW &&
                                  mouseY >= withdrawBtnY && mouseY < withdrawBtnY + withdrawBtnH;

        // Bouton Deposit
        ResourceLocation depositTexture = depositHovered ? BUTTON_DEPOSIT_HOVER : BUTTON_DEPOSIT_NORMAL;
        ImageRenderer.renderScaledImage(g, depositTexture, depositBtnX, depositBtnY, depositBtnW, depositBtnH);

        String depositLabel = translate("erinium_faction.gui.bank.button.deposit");
        int depositTextX = depositBtnX + (depositBtnW - font.width(depositLabel)) / 2;
        g.drawString(font, depositLabel, depositTextX, depositBtnY + sh(9, scaleY), 0xFFffffff, true);

        // Bouton Withdraw
        ResourceLocation withdrawTexture = withdrawHovered ? BUTTON_WITHDRAW_HOVER : BUTTON_WITHDRAW_NORMAL;
        ImageRenderer.renderScaledImage(g, withdrawTexture, withdrawBtnX, withdrawBtnY, withdrawBtnW, withdrawBtnH);

        String withdrawLabel = translate("erinium_faction.gui.bank.button.withdraw");
        int withdrawTextX = withdrawBtnX + (withdrawBtnW - font.width(withdrawLabel)) / 2;
        g.drawString(font, withdrawLabel, withdrawTextX, withdrawBtnY + sh(9, scaleY), 0xFFffffff, true);

        // Titre de section "Transaction History"
        g.drawString(font, translate("erinium_faction.gui.bank.history"), x, y + sh(100, scaleY), 0xFFa0a0c0, false);

        // Transaction scroll list
        transactionScrollList.render(g, mouseX, mouseY);
    }

    @Override
    public void tick() {
        cursorBlink++;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        if (button != 0) return false;

        // Vérifier clic sur input field
        if (mouseX >= inputX && mouseX < inputX + inputW && mouseY >= inputY && mouseY < inputY + inputH) {
            inputFocused = true;
            return true;
        } else {
            inputFocused = false;
        }

        // Bouton Deposit
        if (mouseX >= depositBtnX && mouseX < depositBtnX + depositBtnW &&
            mouseY >= depositBtnY && mouseY < depositBtnY + depositBtnH) {
            handleDeposit();
            return true;
        }

        // Bouton Withdraw
        if (mouseX >= withdrawBtnX && mouseX < withdrawBtnX + withdrawBtnW &&
            mouseY >= withdrawBtnY && mouseY < withdrawBtnY + withdrawBtnH) {
            handleWithdraw();
            return true;
        }

        // Déléguer au scroll list
        if (transactionScrollList != null) {
            return transactionScrollList.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        if (transactionScrollList != null) {
            return transactionScrollList.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (transactionScrollList != null) {
            return transactionScrollList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (transactionScrollList != null) {
            return transactionScrollList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers, int leftPos, int topPos, double scaleX, double scaleY) {
        if (!inputFocused) return false;

        // Backspace
        if (keyCode == InputConstants.KEY_BACKSPACE) {
            if (amountInput.length() > 0) {
                amountInput.deleteCharAt(amountInput.length() - 1);
            }
            return true;
        }

        // Enter - valider le dépôt
        if (keyCode == InputConstants.KEY_RETURN) {
            handleDeposit();
            return true;
        }

        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers, int leftPos, int topPos, double scaleX, double scaleY) {
        if (!inputFocused) return false;

        // Accepter uniquement les chiffres
        if (Character.isDigit(codePoint)) {
            // Limiter la longueur
            if (amountInput.length() < 10) {
                amountInput.append(codePoint);
            }
            return true;
        }

        return false;
    }

    private void handleDeposit() {
        if (amountInput.length() == 0) return;

        try {
            long amount = Long.parseLong(amountInput.toString());
            if (amount <= 0) return;

            // Envoyer packet au serveur pour déposer
            var minecraft = Minecraft.getInstance();
            if (minecraft.getConnection() != null) {
                minecraft.getConnection().send(new ServerboundCustomPayloadPacket(new BankDepositMessage(amount)));
            }

            // Réinitialiser l'input
            amountInput.setLength(0);
        } catch (NumberFormatException e) {
            EFC.log.warn("§6Bank", "§cInvalid amount format: {}", amountInput.toString());
        }
    }

    private void handleWithdraw() {
        if (amountInput.length() == 0) return;

        try {
            long amount = Long.parseLong(amountInput.toString());
            if (amount <= 0) return;

            // Envoyer packet au serveur pour retirer
            var minecraft = Minecraft.getInstance();
            if (minecraft.getConnection() != null) {
                minecraft.getConnection().send(new ServerboundCustomPayloadPacket(new BankWithdrawMessage(amount)));
            }

            // Réinitialiser l'input
            amountInput.setLength(0);
        } catch (NumberFormatException e) {
            EFC.log.warn("§6Bank", "§cInvalid amount format: {}", amountInput.toString());
        }
    }
}

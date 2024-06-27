import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import javax.inject.Inject;

@PluginDescriptor(
    name = "Auto Trader",
    description = "Automatically buys and sells items on the GE to make a profit",
    tags = {"grand exchange", "ge", "auto"}
)
public class AutoTraderPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private AutoTraderConfig config;

    @Inject
    private ItemManager itemManager;

    private boolean buying = true;

    @Override
    protected void startUp() throws Exception
    {
        // Initialization code
    }

    @Override
    protected void shutDown() throws Exception
    {
        // Cleanup code
    }

    @Provides
    AutoTraderConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(AutoTraderConfig.class);
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        clientThread.invoke(() -> {
            if (buying)
            {
                buyItem(config.itemToBuy(), config.buyPrice(), config.quantity());
            }
            else
            {
                sellItem(config.itemToBuy(), config.sellPrice(), config.quantity());
            }
        });
    }

    private void buyItem(String itemName, int price, int quantity)
    {
        int itemId = itemManager.search(itemName).get(0).getId();

        GameObject geBooth = findGEBooth();
        if (geBooth != null)
        {
            interactWithGEBooth(geBooth);

            clientThread.invokeLater(() -> {
                if (openBuyInterface(itemId))
                {
                    setBuyPrice(price);
                    setBuyQuantity(quantity);
                    confirmBuy();
                }
            });
        }
    }

    private void sellItem(String itemName, int price, int quantity)
    {
        int itemId = itemManager.search(itemName).get(0).getId();

        GameObject geBooth = findGEBooth();
        if (geBooth != null)
        {
            interactWithGEBooth(geBooth);

            clientThread.invokeLater(() -> {
                if (openSellInterface(itemId))
                {
                    setSellPrice(price);
                    setSellQuantity(quantity);
                    confirmSell();
                }
            });
        }
    }

    private GameObject findGEBooth()
    {
        for (GameObject gameObject : client.getGameObjects())
        {
            if (gameObject.getId() == ObjectID.GRAND_EXCHANGE_BOOTH)
            {
                return gameObject;
            }
        }
        return null;
    }

    private void interactWithGEBooth(GameObject geBooth)
    {
        client.invokeMenuAction(
            "Exchange", 
            "", 
            geBooth.getId(), 
            MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), 
            geBooth.getSceneMinLocation().getX(), 
            geBooth.getSceneMinLocation().getY()
        );
    }

    private boolean openBuyInterface(int itemId)
    {
        Widget geWidget = client.getWidget(WidgetInfo.GRAND_EXCHANGE_OFFER_CONTAINER);
        if (geWidget != null && !geWidget.isHidden())
        {
            client.invokeMenuAction(
                "Buy", 
                "", 
                -1, 
                MenuAction.CC_OP.getId(), 
                geWidget.getId(), 
                0
            );
            clientThread.invokeLater(() -> searchForItem(itemId));
            return true;
        }
        return false;
    }

    private boolean openSellInterface(int itemId)
    {
        Widget geWidget = client.getWidget(WidgetInfo.GRAND_EXCHANGE_OFFER_CONTAINER);
        if (geWidget != null && !geWidget.isHidden())
        {
            client.invokeMenuAction(
                "Sell", 
                "", 
                -1, 
                MenuAction.CC_OP.getId(), 
                geWidget.getId(), 
                1
            );
            clientThread.invokeLater(() -> selectItemToSell(itemId));
            return true;
        }
        return false;
    }

    private void searchForItem(int itemId)
    {
        Widget searchWidget = client.getWidget(WidgetInfo.GRAND_EXCHANGE_SEARCH_BOX);
        if (searchWidget != null && !searchWidget.isHidden())
        {
            client.invokeMenuAction(
                "Select", 
                "", 
                itemId, 
                MenuAction.CC_OP.getId(), 
                searchWidget.getId(), 
                0
            );
        }
    }

    private void selectItemToSell(int itemId)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null)
        {
            for (Widget item : inventoryWidget.getDynamicChildren())
            {
                if (item.getItemId() == itemId)
                {
                    client.invokeMenuAction(
                        "Offer", 
                        "", 
                        itemId, 
                        MenuAction.CC_OP.getId(), 
                        item.getId(), 
                        0
                    );
                    break;
                }
            }
        }
    }

    private void setBuyPrice(int price)
    {
        Widget priceWidget = client.getWidget(WidgetInfo.GRAND_EXCHANGE_OFFER_PRICE);
        if (priceWidget != null)
        {
            client.invokeMenuAction(
                "Enter price", 
                "", 
                -1, 
                MenuAction.CC_OP.getId(), 
                priceWidget.getId(), 
                0
            );
            clientThread.invokeLater(() -> enterAmount(price));
        }
    }

    private void setSellPrice(int price)
    {
        Widget priceWidget = client.getWidget(WidgetInfo.GRAND_EXCHANGE_OFFER_PRICE);
        if (priceWidget != null)
        {
            client.invokeMenuAction(
                "Enter price", 
                "", 
                -1, 
                MenuAction.CC_OP.getId(), 
                priceWidget.getId(), 
                0
            );
            clientThread.invokeLater(() -> enterAmount(price));
        }
    }

    private void setBuyQuantity(int quantity)
    {
        Widget quantityWidget = client.getWidget(WidgetInfo.GRAND_EXCHANGE_OFFER_QUANTITY);
        if (quantityWidget != null)
        {
            client.invokeMenuAction(
                "Enter quantity", 
                "", 
                -1, 
                MenuAction.CC_OP.getId(), 
                quantityWidget.getId(), 
                0
            );
            clientThread.invokeLater(() -> enterAmount(quantity));
        }
    }

    private void setSellQuantity(int quantity)
    {
        Widget quantityWidget = client.getWidget(WidgetInfo.GRAND_EXCHANGE_OFFER_QUANTITY);
        if (quantityWidget != null)
        {
            client.invokeMenuAction(
                "Enter quantity", 
                "", 
                -1, 
                MenuAction.CC_OP.getId(), 
                quantityWidget.getId(), 
                0
            );
            clientThread.invokeLater(() -> enterAmount(quantity));
        }
    }

    private void confirmBuy()
    {
        Widget confirmWidget = client.getWidget(WidgetInfo.GRAND_EXCHANGE_CONFIRM_BUTTON);
        if (confirmWidget != null)
        {
            client.invokeMenuAction(
                "Confirm", 
                "", 
                -1, 
                MenuAction.CC_OP.getId(), 
                confirmWidget.getId(), 
                0
            );
        }
    }

    private void confirmSell()
    {
        Widget confirmWidget = client.getWidget(WidgetInfo.GRAND_EXCHANGE_CONFIRM_BUTTON);
        if (confirmWidget != null)
        {
            client.invokeMenuAction(
                "Confirm", 
                "", 
                -1, 
                MenuAction.CC_OP.getId(), 
                confirmWidget.getId(), 
                0
            );
        }
    }

    private void enterAmount(int amount)
    {
        // Simulate typing the amount
        client.setVarbit(Varbits.GRAND_EXCHANGE_PRICE, amount);
    }
}

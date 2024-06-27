import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("autotrader")
public interface AutoTraderConfig extends Config
{
    @ConfigItem(
        keyName = "itemToBuy",
        name = "Item to Buy",
        description = "Name of the item to buy on the GE"
    )
    default String itemToBuy()
    {
        return "Coins"; // Default item to buy
    }

    @ConfigItem(
        keyName = "quantity",
        name = "Quantity",
        description = "Quantity of items to buy or sell"
    )
    default int quantity()
    {
        return 100; // Default quantity
    }

    @ConfigItem(
        keyName = "maxStock",
        name = "Max Stock",
        description = "Maximum stock of an item before stopping buying"
    )
    default int maxStock()
    {
        return 1000; // Default max stock
    }
}

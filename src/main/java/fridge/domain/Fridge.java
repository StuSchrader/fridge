package fridge.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Fridge {
    @Id
    private String id;

    private String name;
    private List<Item> items = new ArrayList<>();

    public Fridge(String name) {
        this.name = name;
    }

    public void addItem(Item item, int count) {
        itemsExist();

        for (int i = 0; i < count; i++) {
            items.add(item);
        }
    }

    public void removeItem(Item item, int count) {
        itemsExist();

        Iterator<Item> iterator = items.iterator();
        int removed = 0;
        while (iterator.hasNext()) {
            if (iterator.next().getId().equals(item.getId())) {
                iterator.remove();
                ++ removed;
            }
            if (removed >= count) {
                break;
            }
        }
    }

    public int getItemCount(String itemId) {
        itemsExist();
        return (int)items.stream().filter(item-> item.getId().equals(itemId)).count();
    }

    public int getItemTypeCount(ItemType itemType) {
        itemsExist();

        return (int)items.stream().filter(item -> item.getItemType() == itemType).count();
    }



    private List<Item> itemsExist() {
        if (items == null) {
            items = new ArrayList<>();
        }

        return items;
    }


}

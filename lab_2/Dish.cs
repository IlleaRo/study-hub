namespace lab_2
{
    public enum DishCategories
    {
        Salad,
        Soup,
        Main,
        Dessert,
        Drink
    }
    public class Dish
    {
        int price;
        string name;
        DishCategories category;

        public Dish(int price, string name, DishCategories category)
        {
            this.price = price;
            this.name = name;
            this.category = category;
        }

        public Dish(string price, string name, string category)
        {
            this.price = int.Parse(price);
            this.name = name;
            if (Enum.TryParse(category, true, out this.category) != true)
            {
                throw new Exception("wrong category");
            }
        }

        public string toHTMLString()
        {
            return 
                   "<p>Name: " + this.name + "<p/>" +
                   "<p>Price: " + this.price + "<p/>" +
                   "<p>Category: " + this.category.ToString() + "<p/>";
        }
    }
}

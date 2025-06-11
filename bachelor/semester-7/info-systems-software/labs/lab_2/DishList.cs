namespace lab_2
{
    public static class DishList
    {
        static Dish defaultDish = new Dish(100, "Caesar", DishCategories.Salad);

        static List<Dish> dishList = new List<Dish>
        {
            defaultDish
        };

        static List<Dish> getList() { return dishList; }
        public static void add(Dish dish) { dishList.Add(dish); }
        public static string toHTMLString()
        {
            string result = "<div><hr>";

            foreach (Dish dish in getList())
            {
                result += "<p>" + dish.toHTMLString() + "<p/> <hr>";
            }

            result += "<div/>";

            return result;
        }
    }
}

using Npgsql;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Xml;
using static lab_3.TrashContainer;
using static System.Net.Mime.MediaTypeNames;

namespace lab_3
{
    public class TrashContainer
    {
        public enum TrashType
        {
            // enum with kirillic names))
            Пластик,
            Бумага,
            Картон,
            Стекло,
            Метал
        }

        public int id;
        public string city;
        public string street;
        public int house;
        public HashSet<TrashType> allowedWaste;

        public TrashContainer(int id, string city, string street, int house, HashSet<TrashType> allowedWaste)
        {
            this.id = id;
            this.city = city;
            this.street = street;
            this.house = house;
            this.allowedWaste = allowedWaste;
        }
    }
    public class DatabaseAPI
    {
        private const string connectionString = "Host=forward.feckingpotato.xyz;Port=5432;Database=is_labs;Username=is_labs;Password=is_labs;";
        private const string table = "trash_containers";
        private NpgsqlConnection? connection;

        public DatabaseAPI()
        {
            this.connection = new NpgsqlConnection(connectionString);
        }

        public void AddContainer(TrashContainer container)
        {
            if (connection == null)
            {
                Console.WriteLine("Connection is not established.");
                return;
            }

            connection.Open();

            // Преобразуем массив перечисления в массив строк
            string allowedWasteString;

            if (container.allowedWaste.Count > 0)
            {
                var allowedWasteArray = container.allowedWaste.Select(w => $"'{w}'::trash_type").ToArray();
                allowedWasteString = $"ARRAY[{string.Join(", ", allowedWasteArray)}]";
            }
            else
            {
                allowedWasteString = "null";
            }

            // Формируем строку запроса с заполненными значениями
            string query = $@"
                INSERT INTO {table}(city,street,house,allowed_waste)
                VALUES('{container.city}', 
                       '{container.street}', 
                        {container.house}, 
                        {allowedWasteString});
            ";

            using var cmd = new NpgsqlCommand(query, connection);

            // Выводим содержимое команды
            Console.WriteLine("Command Text: " + cmd.CommandText);

            try
            {
                cmd.ExecuteNonQuery();
                Console.WriteLine("Container added successfully.");
            }
            catch (Exception ex)
            {
                Console.WriteLine("Error occurred while adding container: " + ex.Message);
            }
            finally
            {
                connection.Close();
            }
        }

        public bool FindContainer(int id)
        {
            if (connection == null)
            {
                Console.WriteLine("Connection is not established.");
                return false;
            }

            connection.Open();

            string query = $"SELECT FROM {table} WHERE id = {id};";

            using var cmd = new NpgsqlCommand(query, connection);

            using var reader = cmd.ExecuteReader();

            if (reader.Read())
            {
                return true;
            }

            return false;
        }

        public bool RemoveContainer(int id)
        {
            if (connection == null)
            {
                Console.WriteLine("Connection is not established.");
                return false;
            }

            connection.Open();

            string query = $"DELETE FROM {table} WHERE id = {id};";

            using var cmd = new NpgsqlCommand(query, connection);

            try
            {
                cmd.ExecuteNonQuery();
                Console.WriteLine("Container removed successfully.");
            }
            catch (Exception ex)
            {
                Console.WriteLine("Error occurred while removing container: " + ex.Message);
                return false;
            }
            finally
            {
                connection.Close();
            }

            return true;
        }

        public bool ExportTrash()
        {
            if (connection == null)
            {
                Console.WriteLine("Connection is not established.");
                return false;
            }

            connection.Open();

            string query = $@"
                SELECT database_to_xml(true, true, 'n');
            ";

            using var cmd = new NpgsqlCommand(query, connection);
            using var reader = cmd.ExecuteReader();

            if (reader.Read()) {
                File.WriteAllText("db.xml", reader.GetString(0));
            }

            connection.Close();

            return true;
        }

        private List<TrashContainer> xmlToContainer(XmlNodeList trash_containers)
        {
            const int idIndex = 0;
            const int cityIndex = 1;
            const int streetIndex = 2;
            const int houseIndex = 3;
            const int typeIndex = 4;
            List<TrashContainer> result = new List<TrashContainer>();

            foreach (XmlNode xmlContainer in trash_containers)
            {
                HashSet<TrashType> types = new HashSet<TrashType>();
                XmlDocument xmlTypes = new XmlDocument();
                XmlNodeList xmlTypesNodes;

                xmlTypes.LoadXml(xmlContainer.ChildNodes[typeIndex].OuterXml);
                xmlTypesNodes = xmlTypes.GetElementsByTagName("element");
                foreach(XmlNode xmlTypeNode in xmlTypesNodes)
                {
                    types.Add((TrashType)Enum.Parse(typeof(TrashType), xmlTypeNode.InnerText));
                }

                TrashContainer container = new TrashContainer(
                                                                Int32.Parse(xmlContainer.ChildNodes[idIndex].InnerText),
                                                                xmlContainer.ChildNodes[cityIndex].InnerText,
                                                                xmlContainer.ChildNodes[streetIndex].InnerText,
                                                                Int32.Parse(xmlContainer.ChildNodes[houseIndex].InnerText),
                                                                types
                                                              );
                result.Add(container);
            }
            return result;
        }

        public bool ImportTrash(string PATH)
        {
            XmlDocument database = new XmlDocument();
            XmlNodeList trash_containers;

            database.Load("uploads\\DB.xml");
            trash_containers = database.GetElementsByTagName("trash_containers");


            foreach (var container in GetAllContainers())
            {
                RemoveContainer(container.id);
            }

            foreach (var container in xmlToContainer(trash_containers))
            {
                AddContainer(container);
            }

            
            return true;
        }

        public TrashContainer? GetContiner(int id)
        {
            if (connection == null)
            {
                Console.WriteLine("Connection is not established.");
                return null;
            }

            connection.Open();

            string query = $"SELECT FROM {table} WHERE id = {id};";

            using var cmd = new NpgsqlCommand(query, connection);

            using var reader = cmd.ExecuteReader();

            if (reader.Read())
            {
                string city = reader.GetString(1);
                string street = reader.GetString(2);
                int house = reader.GetInt32(3);
                string[] allowedWasteArray = reader.GetFieldValue<string[]>(4);

                HashSet<TrashType> allowedWasteSet = new HashSet<TrashType>();

                foreach (string waste in allowedWasteArray)
                {
                    allowedWasteSet.Add((TrashType)Enum.Parse(typeof(TrashType), waste));
                }

                connection.Close();

                return new TrashContainer(id, city, street, house, allowedWasteSet);
            }

            return null;
        }

        private List<TrashContainer>? GetAllContainers()
        {
            if (connection == null)
            {
                Console.WriteLine("Connection is not established.");
                return null;
            }

            connection.Open();

            string query = $@"
                SELECT * FROM {table};
            ";

            using var cmd = new NpgsqlCommand(query, connection);
            using var reader = cmd.ExecuteReader();

            List<TrashContainer> containers = [];

            while (reader.Read())
            {
                int id = reader.GetInt32(0);
                string city = reader.GetString(1);
                string street = reader.GetString(2);
                int house = reader.GetInt32(3);
                string[] allowedWasteArray = reader.IsDBNull(4) ? Array.Empty<string>() : reader.GetFieldValue<string[]>(4);

                HashSet<TrashType> allowedWasteSet = [];

                foreach (string waste in allowedWasteArray)
                {
                    allowedWasteSet.Add((TrashType)Enum.Parse(typeof(TrashType), waste));
                }

                containers.Add(new TrashContainer(id, city, street, house, allowedWasteSet));
            }

            connection.Close();

            return containers;
        }

        public List<TrashContainer>? GetContiners(
            string? city = null,
            string? street = null,
            int? house = null,
            HashSet<TrashType>? trashTypes = null)
        {
            if (city is null && street is null && house is null && trashTypes is null)
                return GetAllContainers();
            throw new Exception("not implemented");
        }


        public async Task<bool> UpdateContainerAsync(TrashContainer container)
        {
            if (connection == null)
            {
                Console.WriteLine("Connection is not established.");
                return false;
            }

            await connection.OpenAsync();

            // Преобразуем массив перечисления в массив строк
            string allowedWasteString;

            if (container.allowedWaste.Count > 0)
            {
                var allowedWasteArray = container.allowedWaste.Select(w => $"'{w}'::trash_type").ToArray();
                allowedWasteString = $"ARRAY[{string.Join(", ", allowedWasteArray)}]";
            } else
            {
                allowedWasteString = "null";
            }

            // Формируем строку запроса с заполненными значениями
            string query = $@"
                UPDATE {table} SET
                city = '{container.city}', 
                street = '{container.street}', 
                house = {container.house}, 
                allowed_waste = {allowedWasteString}
                WHERE id = {container.id};
            ";

            using var cmd = new NpgsqlCommand(query, connection);

            // Выводим содержимое команды
            Console.WriteLine("Command Text: " + cmd.CommandText);

            try
            {
                await cmd.ExecuteNonQueryAsync();
                Console.WriteLine("Container updated successfully.");
            }
            catch (Exception ex)
            {
                Console.WriteLine("Error occurred while updating container: " + ex.Message);
                return false;
            }
            finally
            {
                await connection.CloseAsync();
            }

            return true;
        }
    }
}

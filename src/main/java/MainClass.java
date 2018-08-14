/*
Java 3. Базы данных
ДЗ №2: Работа с БД из Java-приложения.

(Решение дополнительной задачи в комментарии после кода основного ДЗ)
Студент: Гришин Дмитрий
 */

import java.sql.*;//библиотека для работы с SQL
import java.util.Scanner;

public class MainClass {
    private static Connection connection; //объект, необходимый для подкл. к БД
    private static Statement stmt; //объект для отправки SQL-запросов из приложения в БД
    private static PreparedStatement pstmt; //объект для подготовленных SQL-запросов
    private static ResultSet rs;
    private static Scanner scanner;//объект сканера для обработки команд пользователя

    public static void main(String[] args) {
        try {
            connect();
            //1. Формирую таблицу товаров запросом
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "prodid INTEGER NOT NULL," +
                    "title TEXT NOT NULL," +
                    "cost INTEGER NOT NULL);");

            //Проверяю, создалась ли таблица:
//            rs = stmt.executeQuery("SELECT * FROM products");//запрос на чтение всех столбцов таблицы
//            ResultSetMetaData rsmd = rs.getMetaData();//спецобъект для получения информации о таблице
//            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
//                System.out.println(rsmd.getColumnName(i)); //вывожу наименование столбца с индексом i
//            }

            //2. Очистка таблицы:
            stmt.execute("DELETE FROM products;");

            //Заполнение таблицы 10000 товаров:
            connection.setAutoCommit(false); //выключаю автосохранение после каждого запроса
            //подготовленный запрос:
            pstmt = connection.prepareStatement("INSERT INTO products (prodid, title, cost) VALUES (?, ?, ?)");
            for (int i = 1; i <= 10000 ; i++) {
                pstmt.setInt(1, i);//инициализирую значение поля prodid (параметр с индексом 1)
                pstmt.setString(2, "товар" + i);
                pstmt.setInt(3, i * 10);
                pstmt.addBatch();//добавляю запрос в пакет
            }
            pstmt.executeBatch();//выполняю пакет запросов одним махом
            connection.setAutoCommit(true);//включаю автосохранение

            //3. Консольное приложение
            userCommunication();

        } catch (ClassNotFoundException e){
            e.printStackTrace();
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            try {
                disconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    //метод connect для установки соединения:
    public static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");//указываем, какую будем использовать JDBC
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");//указываем адрес к БД
        stmt = connection.createStatement();//инициализируем объект stmt в момент подключения к БД
    }

    //метод для закрытия соединения:
    public static void disconnect() throws SQLException {
        connection.close();
    }

    public static void userCommunication() throws SQLException {
        boolean exit = false;
        System.out.print("Введите одну из перечисленных команд и нажмите [Enter]:\n/цена товар<№>\n/сменитьцену товар<№> <новая_цена>\n/товарыпоцене <min_цена> <max_цена>"
                + "\nлюбой введённый символ или некорректная команда - выход из приложения\n");
        scanner = new Scanner(System.in);//сканер для ввода с клавиатуры

        do{
        String userCommand = scanner.nextLine();//считываю команду пользователя
        String[] wds = userCommand.split(" ");//разбиваю сообщение в строчный массив (разделитель пробел)



            switch (wds[0]) {
                case "/цена":
                    pstmt = connection.prepareStatement("SELECT cost FROM products WHERE title = ?");
                    pstmt.setString(1, wds[1]); //устанавливаю параметр для запроса
                    rs = pstmt.executeQuery();//записываю результат выполнения запроса
                    if (!rs.isBeforeFirst()) System.out.println("Такого товара нет");//проверяю, есть ли результат запроса без считывания
                    else {
                        while (rs.next()) {
                            System.out.println(wds[1] + " стоит "+ rs.getInt("cost"));//вывожу результат запроса
                        }
                    }
                    break;
                case "/сменитьцену":
                    pstmt = connection.prepareStatement("UPDATE products SET cost = ? WHERE title = ?");
                    pstmt.setInt(1, Integer.parseInt(wds[2]));
                    pstmt.setString(2, wds[1]);
                    pstmt.execute();//просто выполняю SQL-запрос
                    break;
                case "/товарыпоцене":
                    pstmt = connection.prepareStatement("SELECT prodid, title, cost FROM products WHERE cost BETWEEN ? AND ?");
                    pstmt.setInt(1, Integer.parseInt(wds[1]));
                    pstmt.setInt(2, Integer.parseInt(wds[2]));
                    rs = pstmt.executeQuery();//записываю результат выполнения запроса
                    while (rs.next()) {
                        System.out.println(rs.getInt(1) + " " + rs.getString(2) + " - цена: " + rs.getInt(3));//вывожу результат запроса
                    }
                    break;
                default:
                    System.out.println("Спасибо. До свидания.");
                    exit = true;
            }
        } while (!exit);
    }
}

/*
Дополнительная задача по SQL-запросам:
Сделать выборку, чтобы получить последний вход у каждого пользователя.

Пояснение:
student - столбец с именем студента, entrance_date - столбец с датой входа

Т.к. в SQLite нет типа данных DATETIME, мой запрос выполнен для СУБД MySql:

SELECT student, max(entrance_date) FROM student_entrance GROUP BY student;
 */


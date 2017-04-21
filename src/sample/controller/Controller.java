package sample.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.settings.Config;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {
    // 文件的绝对路径
    private String filePath;
    // 文件夹的局对路径
    private String dirPath;

    @FXML
    Button Submit;

    @FXML
    Button Reset;
    
    @FXML
    TextArea DB;
    
    @FXML
    TextArea Logger;
    
    @FXML
    TextArea Console;
    @FXML
    Button fileChooser;

    @FXML
    Button dirChooser;

    @FXML
    Stage stage;

    @FXML
    public void dirSelector(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择文件夹");
        File file = directoryChooser.showDialog(stage);
        if(file != null){
            dirPath = file.getAbsolutePath();
        }
    }

    @FXML
    public void fileSelector(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择文件");
        File file = fileChooser.showOpenDialog(stage);
        if(file != null){
            filePath = file.getAbsolutePath();
        }
    }
    /**
     * 点击提交按钮进行调用相应的程序进行处理
     */
    @FXML
    public void onButtonClick() {
        Console.setText("查询结果：\n");
        // 调用查询模型与数据库的表对应的函数
        if(!DB.getText().isEmpty()){
            main();
        } else {
            Console.setText("请填加数据库表列表或文件");
        }
    }

    @FXML
    public void onResetClick(){
        Console.setText("");
        Logger.setText("");
        DB.setText("");
    }


    public HashMap<String,Integer> username = new HashMap<>();
    public void main() {
        HashMap<String, Object> map = ModelTableNum();
        URL url = null;
        HashSet<String> set = (HashSet<String>) map.get("set");
        ArrayList<String> list = (ArrayList<String>) map.get("list");
        Console.appendText("========================================================\n");
        Collections.sort(list);
        Console.appendText("set[不重复的模型]："+set.size()+"\n");
        Console.appendText("list[可能重复的模型]："+list.size()+"\n");
        Console.appendText("模型中一共有"+set.size()+"个表\n");
        Console.appendText("检测数据库中的表是否在模型中\n");
        // 把读取回来的信息转化装换成字符串数组
        String [] context = DB.getText().split("\n");
        HashSet<String> set_tmp = dbLoggerndModelCmp(context,set);
        Console.appendText("检查模型中有而数据库中没有的表\n");
        Console.appendText("========================================================\n");
        for(String i:list){
            if(!set_tmp.contains(i.trim())){
                Console.appendText(i+"\n");
            }
        }
        Console.appendText("========================================================\n");
        Console.appendText("模型中的属组名包括："+username+"\n");

    }

    /**
     * 用于计算和记录账管模型中的表的数量
     * @return
     */
    public HashMap<String,Object> ModelTableNum(){
        String root = null;
        if(dirPath == null){
            root = "D:\\tmp";
        } else {
            root = dirPath;
        }
        Set<String> model_set = getModelFiles(root);
        Logger.appendText(dirPath+"\n");
        Path path = Paths.get(root);
        File file = path.toFile();
        File[] files = file.listFiles();
        HashSet<String> set = new HashSet<>();
        ArrayList<String> list = new ArrayList<>();
        HashMap<String,Object> map = new HashMap<>();
        for(File i: files){
            if(model_set.contains(i.getName())){
                String file_path = root+File.separator+i.getName().toString();
                File tmp_file = new File(file_path);
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tmp_file)));
                    String buffer = null;
                    // 循环遍历tmp下的*.sql文件内容
                    int count = 0;
                    while((buffer = br.readLine()) != null){
                        // 定义正则(匹配创建表的的表达式)
                        String regex = "CREATE(.*)TABLE (.*)\\.(.*)[(|\n|  (| (]*";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(buffer.toUpperCase());
                        if(matcher.find()){
                            count++;
                            String user = matcher.group(2);
                            if(username.containsKey(user)){
                                int num = username.get(user);
                                num++;
                                username.put(user,num);
                            } else {
                                username.put(user,1);
                            }
                            String tmp_str = matcher.group(3);
                            //Console.appendText(tmp_str);
                            set.add(tmp_str.trim());
                            list.add(tmp_str.trim());
                        }
                    }
                    Logger.appendText(i.getName()+"的表数量是"+count+"\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        map.put("set",set);
        map.put("list",list);
        return map;
    }

    private Set getModelFiles(String dirPath){
        HashSet<String> set = new HashSet<>();
        InputStream inputStream = Controller.class.getResourceAsStream(Config.FILES);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String buffer = null;
            while ((buffer = reader.readLine())!=null){
                set.add(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return set;
    }

    /**
     * 用于比较数据库和账管模型的表数量
     * @param db_file 数据库中的表名称的文件名
     * @return 返回生产库中的表
     */
    public HashSet<String> dbLoggerndModelCmp(String [] db_file,HashSet<String> set){
        ArrayList<String> list = new ArrayList<>();
        HashSet<String> set_tmp = new HashSet<>();
        try {
            if(db_file.length==0){
                Console.appendText("请添加数据库的表表名");
            }
            for(String buffer:db_file){
                list.add(buffer.trim());
                set_tmp.add(buffer.trim());
            }
            int count = 0;
            Console.appendText("========================================================\n");
            Console.appendText("DB库中有但是模型中没有的表是：\n");
            for (String i:list){
                if(!set.contains(i)){
                    count++;
                    Console.appendText(i);
                }
            }
            Console.appendText("========================================================\n");
            Console.appendText("一共有"+count+"个表不存在\n");
            Console.appendText("========================================================\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return set_tmp;
    }
}

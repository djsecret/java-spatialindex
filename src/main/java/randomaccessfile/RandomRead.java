package randomaccessfile;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Read data from file at specific location use RandomAccessFile
 * Created by ubuntu on 14-11-29.
 */
public class RandomRead
{

    /**
     * 随机读取文件内容
     *
     * @param fileName 文件名
     */
    public static void readFileByRandomAccess(String fileName)
    {
        RandomAccessFile randomFile = null;
        try
        {
            System.out.println("readFileByRandomAccess:");
            randomFile = new RandomAccessFile(fileName, "r");
            long fileLength = randomFile.length();
            int beginIndex = (fileLength > 4) ? 4 : 0;
            randomFile.seek(beginIndex);
            System.out.println(new String(randomFile.readLine().getBytes("UTF-8"),"GBK"));
            //System.out.println("11;2014/11/29 9:43:19;pac10141113-clean;70.13247;40.30872;16.006 ;28.258 ;good;Utf-8".length());
            System.out.println("12;2014/11/29 9:43:19;pac10141113-clean;71.1502;41.06893;17.092 ;27.450 ;good;Utf-7".length());

        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (randomFile != null)
            {
                try
                {
                    randomFile.close();
                } catch (IOException ignored)
                {
                }
            }
        }
    }


    public static void main(String[] args)
    {
        RandomRead.readFileByRandomAccess("testdata.csv");
    }

}

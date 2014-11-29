package spatialindex.rtree;

import spatialindex.spatialindex.*;
import spatialindex.storagemanager.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

public class RTreeTest
{
    public static void main(String[] args) throws IOException
    {

        /********************init R-tree**********************/
        // Create a disk based storage manager.
        PropertySet ps = new PropertySet();

        ps.setProperty("Overwrite", true);
        //overwrite the file if it exists.

        ps.setProperty("FileName", "tree");
        // .idx and .dat extensions will be added.

        ps.setProperty("PageSize", 4096);
        // specify the page size. Since the index may also contain user defined data
        // there is no way to know how big a single node may become. The storage manager
        // will use multiple pages per node if needed. Of course this will slow down performance.

        IStorageManager diskfile = new DiskStorageManager(ps);

        IBuffer file = new RandomEvictionsBuffer(diskfile, 10, false);

        // Create a new, empty, RTree with dimensionality 3, minimum load 70%, using "file" as
        // the StorageManager and the RSTAR splitting policy.
        PropertySet ps2 = new PropertySet();

        Double f = 0.7;
        ps2.setProperty("FillFactor", f);


        ps2.setProperty("IndexCapacity", 4);
        ps2.setProperty("LeafCapacity", 4);
        // Index capacity and leaf capacity may be different.

        ps2.setProperty("Dimension", 3);

        ISpatialIndex tree = new RTree(ps2, file);


        /********************insert into R-tree**********************/

        String dataFile = "data.csv";
        LineNumberReader lr = null;

        try
        {
            lr = new LineNumberReader(new FileReader(dataFile));
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Cannot open query file " + dataFile + ".");
            System.exit(-1);
        }


        int count = 0;
        int id;
        String datetime;
        String source;
        double lng,lat,temp,salt;
        String quality,encoding;

        double[] pointData = new double[3];

        long start = System.currentTimeMillis();
        String line = lr.readLine();

        while (line != null)
        {
            StringTokenizer st = new StringTokenizer(line,",");
            id = new Integer(st.nextToken());
            datetime = st.nextToken().trim();
            source = st.nextToken().trim();
            lng = new Double(st.nextToken());
            lat = new Double(st.nextToken());
            temp = new Double(st.nextToken());
            salt = new Double(st.nextToken());
            quality = st.nextToken().trim();
            encoding = st.nextToken().trim();
            System.out.println("id:" + id + ",datetime:" + datetime.toString() + ",source:" + source + ",lng:" + lng + ",lat:" + lat + ",temp:" + temp + ",salt:" + salt + ",quality:" + quality + ",encoding:" + encoding);

            SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy／MM／dd hh:mm:ss");// the "/" is chinese!
            java.util.Date date = null;
            try {
                date = myFormatter.parse(datetime);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long time = 0;
            if (date != null)
            {
                time = date.getTime() / 1000;//second since January 1, 1970, 00:00:00 GMT
            }
            System.out.println(time);

            pointData[0] = lng;
            pointData[1] = lat;
            pointData[2] = time;

            Point point = new Point(pointData);

            String data = "source:" + source + ",temp:" + temp + ",salt:" + salt + ",quality:" + quality + ",encoding:" + encoding;
            tree.insertData(data.getBytes(), point, id);

            if ((count % 1000) == 0) System.err.println(count);
            count++;
            line = lr.readLine();
        }

        System.err.println(tree);
        Integer indexID = (Integer) ps2.getProperty("IndexIdentifier");
        System.err.println("Index ID: " + indexID);

        boolean ret = tree.isIndexValid();
        if (!ret) System.err.println("Structure is INVALID!");

        // flush all pending changes to persistent storage (needed since Java might not call finalize when JVM exits).
        tree.flush();

        /********************query from R-tree**********************/

        double[] data1 = {70.13247,40.30872,1417225399};
        double[] data2 = {71.1502,41.06893,1417225399};
        double[] data3 = {70.64253,41.26596,1417225399};
        double[] data4 = {70.64253,41.26596,123};

        Point point1 = new Point(data1);
        Point point2 = new Point(data2);
        Point point3 = new Point(data3);
        Point point4 = new Point(data4);

        MyVisitor vis = new MyVisitor();
        tree.intersectionQuery(point1, vis);
        tree.intersectionQuery(point2, vis);
        tree.intersectionQuery(point3, vis);
        tree.intersectionQuery(point4, vis);
//        tree.intersectionQuery(point5, vis);
//
//        double[] tempData = {0.9,0.9,0.9};
//        Point tempP = new Point(tempData);
//        tree.intersectionQuery(tempP, vis);
//
//
//        // Create a disk based storage manager.
//        PropertySet ps3 = new PropertySet();
//
//        ps3.setProperty("FileName", "tree");
//        // .idx and .dat extensions will be added.
//
//        IStorageManager diskfile1 = new DiskStorageManager(ps3);
//
//        IBuffer file1 = new RandomEvictionsBuffer(diskfile1, 10, false);
//        // applies a main memory random buffer on top of the persistent storage manager
//        // (LRU buffer, etc can be created the same way).
//
//        PropertySet ps4 = new PropertySet();
//
//        // If we need to open an existing tree stored in the storage manager, we only
//        // have to specify the index identifier as follows
//        Integer i = new Integer(1); // INDEX_IDENTIFIER_GOES_HERE (suppose I know that in this case it is equal to 1);
//        ps4.setProperty("IndexIdentifier", i);
//        // this will try to locate and open an already existing r-tree index from file manager file.
//
//        ISpatialIndex tree1 = new RTree(ps4, file1);
//        System.err.println(tree1);
//        tree.intersectionQuery(point1, vis);
//        tree.intersectionQuery(point2, vis);
//        tree.intersectionQuery(point3, vis);
//        tree.intersectionQuery(point4, vis);
//        tree.intersectionQuery(point5, vis);

    }


}

class MyVisitor implements IVisitor
{
    public void visitNode(final INode n) {}

    public void visitData(final IData d)
    {
        System.out.print("query result:");
        System.out.println(d.getIdentifier());
        System.out.println(d.getShape().toString());

        byte[] bytes = d.getData();
        if(bytes != null)
        {
            System.out.println(new String(bytes));
        }
        else
        {
            System.out.println("null");
        }

        // the ID of this data entry is an answer to the query. I will just print it to stdout.
    }
}
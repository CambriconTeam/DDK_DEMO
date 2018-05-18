package com.cambricon.productdisplay.utils;

import com.cambricon.productdisplay.R;
import com.cambricon.productdisplay.bean.News;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gzb on 18-4-2.
 */

public class NewsUtil {

    private static ArrayList<News> newsList = new ArrayList<>();

    public NewsUtil() {
        throw new UnsupportedOperationException("...");
    }

    public static List<News> getList() {
        newsList.clear();
        newsList.add(new News(1,R.drawable.news07,"寒武纪2018产品发布会，5月3日邀您见证","2018年4月22日",
                "https://mp.weixin.qq.com/s/n2ME66EMU0AcAbcANjUEgw"));
        newsList.add(new News(2,R.drawable.news06,"陈云霁研究员参加第十三届全国政协第一次双周协商座谈会","2018年4月21日",
                "https://mp.weixin.qq.com/s/MHpnu2dSlVPk7_HoVe9JHA"));
        newsList.add(new News(3, R.drawable.news05, "全国政协副主席、致公党中央主席、中国科学技术协会主席万钢莅临寒武纪科技调研", "2018年4月10日",
                "https://mp.weixin.qq.com/s/4KzOYxPr_eVCXpt6JH5How"));
        newsList.add(new News(4, R.drawable.news01, "中科院党组副书记、副院长刘伟平调研寒武纪科技", "2018年2月27日",
                "http://mp.weixin.qq.com/s?__biz=MzIwOTM3NDcxNQ==&mid=2247484117&idx=1&sn=f8eb735036c18d446ecb203941f6797d&chksm=97759e41a0021757d80ecf5ea9cb8b8d1a84a581b3b3967911581317b1e11895576d09656870&mpshare=1&scene=23&srcid=0402cy1RUKY43PB4dK5s55Vw#rd\n"));
        newsList.add(new News(5, R.drawable.news02, "【2018寒武纪年会】从伟大创业公司到伟大公司的征途", "2018年2月12日",
                "http://mp.weixin.qq.com/s/qMj8M6xMaxXY6YuaUFXqNw\n"));
        newsList.add(new News(6, R.drawable.news03, "上海证券交易所张冬科副理事长来寒武纪调研", "2018年2月2日",
                "http://mp.weixin.qq.com/s/xZ_gD68gqVcOfilOjyxUwQ\n"));
        newsList.add(new News(7, R.drawable.news05, "寒武纪创始人高票入选央视科技创新人物", "2018年1月21日",
                "http://mp.weixin.qq.com/s/z_oqjRPIO3HCL-uZlym6vg\n"));
        newsList.add(new News(8, R.drawable.news5, "央视年末盘点2017黑科技，寒武纪芯片主力智能时代", "2018年1月1日",
                "http://mp.weixin.qq.com/s?__biz=MzIwOTM3NDcxNQ==&mid=2247484083&idx=1&sn=ac8fac2ae228c8ce328d1a20d925f618&chksm=97759e27a0021731fc5016f5bc7e14c223d7037e155ea3f3462fba34ee60de7d5fc0cf253bca&mpshare=1&scene=23&srcid=0201h9a9R87nRbNZOc8T20mx#rd\n"));
        newsList.add(new News(9, R.drawable.news1, "CB lnsights 最新发布全球AI 100榜单,寒武纪首度入选", "2017年12月19日",
                "http://mp.weixin.qq.com/s?__biz=MzIwOTM3NDcxNQ==&mid=2247484072&idx=1&sn=1f59bc3d0e00f017c4d1462275e07810&chksm=97759e3ca002172aacbeb80f0d8240cd77a728d0676b3c08da872b595d68d90577a76d4d5938&mpshare=1&scene=23&srcid=0201yeAOaOULwORmRuKWM8gY#rd\n"));
        newsList.add(new News(10, R.drawable.news3, "重磅|寒武纪捷报频传--再获两项荣誉", "2017年12月5日",
                "http://mp.weixin.qq.com/s?__biz=MzIwOTM3NDcxNQ==&mid=2247484061&idx=1&sn=a5426e92d04556edbe50bf9faf3d5646&chksm=97759e09a002171fa68b0281cc7c3da7cab7a28e74ca1251a6a4f630f586baf8df9db2d1c1f7&mpshare=1&scene=23&srcid=02013br22Ve8RSzwmauPTH0i#rd\n"));
        newsList.add(new News(11, R.drawable.news4, "寒武纪科技收场发布会回顾", "2017年11月8日",
                "http://mp.weixin.qq.com/s?__biz=MzIwOTM3NDcxNQ==&mid=2247484044&idx=1&sn=16140175ff1b2d848983ee862d8c720c&chksm=97759e18a002170e357197aa5e342910f8404ed5006b9b75d8b3fcbfb85c0702cb75afc8d176&mpshare=1&scene=23&srcid=0201AblJ1T1xUDPzwEz3wBjA#rd\n"));
        newsList.add(new News(12, R.drawable.news2, "寒武纪成功举办第22届国际体系结构...", "2017年4月15日",
                "http://mp.weixin.qq.com/s?__biz=MzIwOTM3NDcxNQ==&mid=2247483879&idx=1&sn=eb46f7cabe394f387f5657e060ff429d&chksm=97759d73a0021465cc7388598fb422566e1abf4b73b2846f64f9665c159e123b5e151f4ae293&mpshare=1&scene=23&srcid=0201wq0DSNP5Adx2Z1nknJDF#rd\n"));

        return newsList;
    }


}

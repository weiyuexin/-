package top.weiyuexin.controller;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import top.weiyuexin.entity.LoginLog;
import top.weiyuexin.entity.User;
import top.weiyuexin.entity.vo.R;
import top.weiyuexin.mapper.UserMapper;
import top.weiyuexin.service.LoginLogService;
import top.weiyuexin.service.UserService;
import top.weiyuexin.utils.IpUtil;
import top.weiyuexin.utils.IpdbUtil;
import top.weiyuexin.utils.getClentIp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    //注入服务
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LoginLogService loginLogService;

    //登录页面
    @GetMapping("/login")
    public String loginPage(){
        return "user/login";
    }

    /**
     * 执行登录操作
     * @param username
     * @param password
     * @param session
     * @return
     */
    @GetMapping("/login.do/{username}/{password}")
    @ResponseBody
    public Object login(@PathVariable("username") String username,
                        @PathVariable("password") String password,
                        HttpSession session){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        R r = new R();
        //对密码进行md5加密处理
        password = DigestUtil.md5Hex(password);
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        lqw.eq(User::getUsername,username).eq(User::getPassword,password);
        User user = userMapper.selectOne(lqw);
        //判断时候查到用户
        if(user!=null){ //查询到了用户
            //更新状态码
            r.setFlag(true);
            r.setMsg("登录成功");
            //登录成功，讲用户信息保存到session
            session.setAttribute("user",user);
            //登录日志
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(user.getId());
            loginLog.setIp(getClentIp.getIpAddr(request));

            System.out.println(loginLog.getIp());

            String addr =  Arrays.toString(IpdbUtil.find(getClentIp.getIpAddr(request), "CN"));
            addr = addr.substring(1,addr.length()-1);
            loginLog.setAddress(addr);
            System.out.println(addr);

            String country = Arrays.toString(new String[]{IpdbUtil.find(getClentIp.getIpAddr(request), "CN")[0]});
            country = country.substring(1,country.length()-1);
            loginLog.setCountry(country);

            String province = Arrays.toString(new String[]{IpdbUtil.find(getClentIp.getIpAddr(request), "CN")[1]});
            province = province.substring(1,province.length()-1);
            loginLog.setProvince(province);
            String city = Arrays.toString(new String[]{IpdbUtil.find(getClentIp.getIpAddr(request), "CN")[2]});
            city = city.substring(1,city.length()-1);
            loginLog.setCity(city);
            //获取时间
            java.util.Date time=new Date();
            SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(sdf.format(time));
            loginLog.setTime(time);
            loginLogService.save(loginLog);

        }else {  //没有查询到用户
            r.setFlag(false);
            r.setMsg("账号或密码错误，请重试!");
        }
        return r;
    }

    /**
     * 邮箱验证码登录
     * @param code
     * @param email
     * @param session
     * @return
     */
    @PostMapping("/loginByEmail/{email}/{code}")
    @ResponseBody
    public R loginByEmail(@PathVariable("code") String code,
                          @PathVariable("email") String email,
                          HttpSession session){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        R r = new R();
        User user = userService.getByEmail(email);
        if(user!=null){
            if(session.getAttribute("code").toString().equals(code)){
                r.setFlag(true);
                r.setMsg("登录成功");
                session.setAttribute("user",user);

                //登录日志
                LoginLog loginLog = new LoginLog();
                loginLog.setUserId(user.getId());
                loginLog.setIp(getClentIp.getIpAddr(request));

                System.out.println(loginLog.getIp());

                String addr =  Arrays.toString(IpdbUtil.find(getClentIp.getIpAddr(request), "CN"));
                addr = addr.substring(1,addr.length()-1);
                loginLog.setAddress(addr);
                System.out.println(addr);

                String country = Arrays.toString(new String[]{IpdbUtil.find(getClentIp.getIpAddr(request), "CN")[0]});
                country = country.substring(1,country.length()-1);
                loginLog.setCountry(country);

                String province = Arrays.toString(new String[]{IpdbUtil.find(getClentIp.getIpAddr(request), "CN")[1]});
                province = province.substring(1,province.length()-1);
                loginLog.setProvince(province);
                String city = Arrays.toString(new String[]{IpdbUtil.find(getClentIp.getIpAddr(request), "CN")[2]});
                city = city.substring(1,city.length()-1);
                loginLog.setCity(city);
                //获取时间
                java.util.Date time=new Date();
                SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.out.println(sdf.format(time));
                loginLog.setTime(time);
                loginLogService.save(loginLog);
            }else {
                r.setMsg("验证码错误，请重试!");
                r.setFlag(false);
            }
        }else {
            r.setFlag(false);
            r.setMsg("该邮箱还未注册,请先前往注册!");
        }

        return r;
    }

    /**
     * 注册页面
     * @return
     */
    @GetMapping("/register")
    public String registerPage(){
        return "user/register";
    }

    /**
     * 注册
     * @param user
     * @param code
     * @param session
     * @return
     */
    @PostMapping ("/register.do/{code}")
    @ResponseBody
    public Object register(User user,
                           @PathVariable("code") Integer code,
                           HttpSession session){
        R r = new R();
        //对密码进行md5加密处理
        user.setPassword(DigestUtil.md5Hex(user.getPassword()));
        user.setAddress("暂无");
        user.setSignature("暂无");
        user.setSex("暂无");
        user.setPhoto("https://wyx-1303917755.cos.ap-beijing.myqcloud.com/img/2022/5/18/2022518f0f23093-5b30-4c45-8e54-cdd71683020a.png");
        Date date = new Date();
        user.setTime(date);
        //验证验证码是否输入正确
        System.out.println(code+" " +session.getAttribute("code"));
        if(!code.equals(session.getAttribute("code"))){
            r.setMsg("验证码错误，请重试!");
        }else{
            //保存到数据库
            user.setEmail(user.getEmail());
            user.setUsername(user.getUsername());
            user.setPassword(user.getPassword());
            if(userService.save(user)){
                r.setFlag(true);
                r.setMsg("注册成功!");
            }else {
                r.setFlag(true);
                r.setMsg("注册失败,请重试!");
            }
        }
        return r;
    }

    /**
     * 检查用户登录状态
     * @param session
     * @return
     */
    @GetMapping("/check")
    @ResponseBody
    public Object checkIsNotLogin(HttpSession session){
        R r = new R();
        //获取session中保存的用户信息
        User user = (User) session.getAttribute("user");
        if (user == null) {
            //未登录
            r.setFlag(false);
            r.setMsg("您还没有登录，请前往登录");
        } else {
            //已登录
            r.setFlag(true);
            r.setMsg("您处于登录状态");
            r.setData(user);
        }

        return r;
    }

    /**
     * 根据id查询文章作者信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ModelAndView getById(@PathVariable("id") Integer id){
        ModelAndView modelAndView = new ModelAndView();
        //查询用户
        User user = userService.getById(id);

        //格式化时间
        SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = time.format(user.getTime());
        //设置试图
        modelAndView.setViewName("user/author");
        //设置内容
        modelAndView.addObject("user",user);
        modelAndView.addObject("date",date);

        return modelAndView;
    }

    /**
     * 退出登录
     * @param session
     * @return
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        //销毁session
        session.invalidate();
        //重新加载首页
        return "redirect:/";
    }


    /**
     * 获取用户积分排行
     * @param num
     * @return
     */
    @GetMapping("/topUser/{num}")
    @ResponseBody
    public R getTopUser(@PathVariable("num") Integer num){
        R r = new R();
        List<User> users = userService.getTopUser(num);
        if(users!=null){
            r.setFlag(true);
            r.setData(users);
            r.setMsg("查询成功");
        }else {
            r.setFlag(false);
            r.setMsg("查询失败，请稍后重试!");
        }

        return r;
    }


    /**
     * 账号管理页面
     * @param t
     * @return
     */
    @GetMapping("/account/{t}")
    public String accountPage(@PathVariable("t") String t){
        if(t.equals("account")){
            return "user/account";
        }else if(t.equals("article")){
            return "article/myarticle";
        }else {
            return "resource/myres";
        }
    }

    /**
     * 查看session中的用户登录信息
     * @param session
     * @return
     */
    @GetMapping("/info")
    @ResponseBody
    public R getUserInfoFromSession(HttpSession session){
        R r = new R();
        User user = (User) session.getAttribute("user");
        if(user!=null){
            r.setFlag(true);
            r.setData(user);
            r.setMsg("已登录");
        }else {
            r.setFlag(false);
            r.setMsg("还没有登录，请前往登录!");
        }
        return r;
    }

    /**
     * 修改用户信息接口
     * @param user
     * @param session
     * @return
     */
    @PutMapping("/update")
    @ResponseBody
    public R updata(User user,HttpSession session){
        R r = new R();
        User user1 =(User) session.getAttribute("user");
        if(session.getAttribute("user")!=null){
            user.setId(user1.getId());
            if(user.getPassword()!=null){
                //加密
                user.setPassword(DigestUtil.md5Hex(user.getPassword()));
            }
            r.setFlag(userService.updateById(user));
            if(r.getFlag()){
                r.setMsg("修改成功!");
                //修改成功，更新session中的信息
                user = userService.getById(user.getId());
                session.setAttribute("user",user);
            }else {
                r.setMsg("修改失败，请稍后重试!");
            }
        }else {
            r.setFlag(false);
            r.setMsg("请登录后重试！");
        }
        return r;
    }

}

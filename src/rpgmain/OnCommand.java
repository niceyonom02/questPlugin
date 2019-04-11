package rpgmain;

import inventory.ScrollManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import scroll.Scroll;

import java.util.ArrayList;
import java.util.Random;

public class OnCommand implements CommandExecutor {
    private RPGMain instance;
    private ScrollManager scrollManager;

    public OnCommand(RPGMain instance, ScrollManager scrollManager){
        this.instance = instance;
        this.scrollManager = scrollManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arg){

        if(sender instanceof ConsoleCommandSender){
            if(label.equalsIgnoreCase("강화")){
                if(arg.length < 1){
                    return false;
                }

                Player target = Bukkit.getPlayer(arg[0]);

                if(target == null){
                    sender.sendMessage("이 플레이어는 온라인이 아닙니다!");
                    return false;
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp scroll " + target.getName());
                return true;
            }
            if(label.equalsIgnoreCase("스크롤")){
                if(arg.length < 1){
                    return false;
                }

                if(arg[0].equalsIgnoreCase("리로드")){
                    //scrollManager.saveScroll();
                    instance.reloadConfig();
                    scrollManager.clearScrolls();
                    scrollManager.loadScroll();

                    instance.yamlReload();
                    return true;
                }

                if(arg[0].equalsIgnoreCase("주기")){
                    if(arg.length < 3){
                        return false;
                    }
                    try{
                        int seenNumber = Integer.parseInt(arg[1]);
                        Player target = Bukkit.getPlayer(arg[2]);
                        scrollManager.givePlayerScroll(target, seenNumber);
                        return true;
                    }catch(NumberFormatException e){
                        sender.sendMessage("/스크롤 주기 [숫자] 닉네임");
                        return false;
                    } catch (NullPointerException e){
                        sender.sendMessage("해당 유저는 온라인이 아닙니다!");
                        return false;
                    }
                }

                if(arg[0].equalsIgnoreCase("랜덤주기")){
                    if(arg.length < 2){
                        sender.sendMessage("/스크롤 랜덤주기 [닉네임]");
                        return false;
                    }
                    try{
                        Player target = Bukkit.getPlayer(arg[1]);
                        ArrayList<Scroll> scrollList = scrollManager.getScrollList();

                        if(scrollList.isEmpty()){
                            return false;
                        }

                        Random ran = new Random();
                        int t = 0;

                        if(scrollList.size() < 10){
                            t = ran.nextInt(scrollList.size());
                        } else{
                            t = ran.nextInt(9);
                        }

                        target.getInventory().addItem(scrollList.get(t).getItemStack());
                        target.sendMessage("§e§l스크롤을 지급받았습니다!");
                        return true;
                    }catch (NullPointerException e){
                        sender.sendMessage("해당 유저는 오프라인입니다!");
                        return false;
                    }
                }
            }
        }

        if(!(sender instanceof Player)){
            return false;
        }
        Player player = (Player)sender;

        if(label.equalsIgnoreCase("스크롤")){
            if(player.isOp()){
                if(arg.length < 1){
                    adminHelp(player);
                    return false;
                }

                if(arg[0].equalsIgnoreCase("랜덤주기")){
                    if(arg.length < 2){
                        sender.sendMessage("/스크롤 랜덤주기 [닉네임]");
                        return false;
                    }
                    try{
                        Player target = Bukkit.getPlayer(arg[1]);
                        ArrayList<Scroll> scrollList = scrollManager.getScrollList();

                        if(scrollList.isEmpty()){
                            return false;
                        }

                        Random ran = new Random();
                        int t = 0;

                        if(scrollList.size() < 10){
                            t = ran.nextInt(scrollList.size());
                        } else{
                            t = ran.nextInt(9);
                        }

                        target.getInventory().addItem(scrollList.get(t).getItemStack());
                        target.sendMessage("§e§l스크롤을 지급받았습니다!");
                        return true;
                    }catch (NullPointerException e){
                        sender.sendMessage("해당 유저는 오프라인입니다!");
                        return false;
                    }
                }

                if(arg[0].equalsIgnoreCase("리스트")){
                    scrollManager.showScrollList(player);
                    return true;
                }

                if(arg[0].equalsIgnoreCase("삭제")){
                    if(arg.length < 2){
                        return false;
                    }
                    try{
                        int seenNumber = Integer.parseInt(arg[1]);
                        scrollManager.deleteScroll(player, seenNumber);
                    }catch(NumberFormatException e){
                        player.sendMessage("/스크롤 삭제 [숫자]");
                        return false;
                    }
                    return true;
                }

                if(arg[0].equalsIgnoreCase("주기")){
                    if(arg.length < 3){
                        return false;
                    }
                    try{
                        int seenNumber = Integer.parseInt(arg[1]);
                        Player target = Bukkit.getPlayer(arg[2]);
                        scrollManager.givePlayerScroll(target, seenNumber);
                        return true;
                    }catch(NumberFormatException e){
                        player.sendMessage("/스크롤 받기 [숫자] 닉네임");
                        return false;
                    } catch (NullPointerException e){
                        player.sendMessage("해당 유저는 온라인이 아닙니다!");
                        return false;
                    }
                }

                if(arg[0].equalsIgnoreCase("리로드")){
                    //scrollManager.saveScroll();
                    instance.reloadConfig();
                    scrollManager.clearScrolls();
                    scrollManager.loadScroll();

                    instance.yamlReload();
                    return true;
                }

                if(arg[0].equalsIgnoreCase("제작")){
                    scrollManager.makeScroll(player);
                    return true;
                }
                return false;
            } else{
                player.sendMessage("§fUnknown command. Type \"/help\" for help.");
                return false;
            }
        }

        if(label.equalsIgnoreCase("강화gui")){
            Inventory reinforcement = Bukkit.createInventory(null, 0, "§0스크롤을 아이템 위에 겹쳐주세요.");
            player.openInventory(reinforcement);
            return true;
        }

        if(label.equalsIgnoreCase("강화")){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp scroll " + player.getName());
            return true;
        }

        return false;
    }

    public void adminHelp(Player player){
        player.sendMessage("/스크롤 리스트");
        player.sendMessage("/스크롤 제작");
        player.sendMessage("/스크롤 받기 [숫자]");
        player.sendMessage("/스크롤 삭제 [숫자]");
        player.sendMessage("/스크롤 리로드");
    }

}

-- ============================================================
-- LUMINA 商品种子数据（插入前先清理测试商品，已有8条不动）
-- 用法: mysql -u root -p123456 --default-character-set=utf8mb4 lumina_mall < seed_products.sql
-- ============================================================

USE lumina_mall;

-- 删除测试商品
DELETE FROM l_product WHERE id = 9999;

-- ============================================================
-- 女装 (category_id=1) — 已有: 真丝斜纹衬衫, 羊皮乐福鞋
-- ============================================================
INSERT INTO l_product (name, description, category_id, price, original_price, stock, badge, badge_text, image, sales, is_hot, is_new, sort_order) VALUES
('法式复古碎花连衣裙', '轻盈雪纺面料，V领设计修饰颈部线条，腰部系带收腰显瘦。浪漫碎花印花，春日约会首选。', 1, 459.00, 599.00, 25, 'sale', '限时特惠', 'https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=600', 128, 1, 0, 1),
('羊绒圆领针织衫', '100%内蒙古山羊绒，软糯亲肤不扎人。宽松版型不挑身材，秋冬叠穿神器。', 1, 899.00, 1299.00, 15, 'hot', '热卖爆款', 'https://images.unsplash.com/photo-1434389677669-e08b4cda3ab4?w=600', 312, 1, 0, 2),
('高腰阔腿西裤', 'TR混纺面料挺括有型，高腰设计拉长腿部比例。商务通勤与日常休闲两穿。', 1, 459.00, NULL, 30, 'new', '新品上市', 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=600', 89, 0, 1, 3),
('桑蚕丝吊带睡裙', '19姆米重磅桑蚕丝，珍珠光泽感，法式蕾丝拼接。宠爱自己的夜晚。', 1, 699.00, 899.00, 12, NULL, '', 'https://images.unsplash.com/photo-1581044777550-4cfa60707c03?w=600', 56, 0, 1, 4),
('棉麻宽松衬衫裙', '天然棉麻混纺透气吸汗，简约衬衫式连衣裙。配腰带可两穿，夏日慵懒风。', 1, 369.00, NULL, 20, 'new', '新品', 'https://images.unsplash.com/photo-1591369822096-ffd140ec948f?w=600', 45, 0, 1, 5),
('双面呢羊毛大衣', '澳大利亚美利奴羊毛双面呢，手工缝制。经典驼色翻领，一件穿十年不过时。', 1, 1899.00, 2599.00, 8, 'hot', '经典款', 'https://images.unsplash.com/photo-1539533018447-63fcce2678e3?w=600', 203, 1, 0, 6),
('弹力修身牛仔裤', '土耳其进口弹力牛仔面料，中高腰收腹设计。微喇裤脚复古时髦。', 1, 499.00, NULL, 35, NULL, '', 'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600', 167, 0, 0, 7);

-- ============================================================
-- 男装 (category_id=2) — 已有: 亚麻宽松西装, 有机棉休闲T恤
-- ============================================================
INSERT INTO l_product (name, description, category_id, price, original_price, stock, badge, badge_text, image, sales, is_hot, is_new, sort_order) VALUES
('埃及棉免烫衬衫', '100支双股埃及长绒棉，液氨免烫工艺。修身版型，商务精英标配。', 2, 599.00, 799.00, 25, 'hot', '回购王', 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=600', 278, 1, 0, 1),
('美利奴羊毛Polo衫', '17.5微米超细美利奴羊毛，吸湿排汗可机洗。商务休闲两穿。', 2, 799.00, NULL, 18, 'new', '新品', 'https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=600', 93, 0, 1, 2),
('弹力修身斜纹裤', '日本进口弹力斜纹面料，360度自由拉伸。通勤久坐不紧绷。', 2, 529.00, 699.00, 20, 'sale', '限时特惠', 'https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=600', 156, 0, 0, 3),
('超轻羽绒马甲', '90%白鹅绒填充，整件仅重180g。可收纳至随身袋，秋冬必备叠穿单品。', 2, 699.00, 999.00, 15, 'hot', '冬季爆款', 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600', 421, 1, 0, 4),
('复古飞行员夹克', '尼龙66高密度面料防风防水，橙色内里亮眼撞色。经典MA-1版型。', 2, 899.00, 1199.00, 10, NULL, '', 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600', 112, 0, 0, 5),
('冰氧吧速干T恤', '凉感纤维面料接触即凉，UPF50+防晒。运动通勤一件搞定。', 2, 199.00, NULL, 50, 'new', '夏日必入', 'https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=600', 89, 0, 1, 6),
('头层牛皮商务腰带', '意大利进口头层植鞣牛皮，黄铜扣件。简约设计不挑场合。', 2, 459.00, NULL, 30, NULL, '', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600', 198, 0, 0, 7);

-- ============================================================
-- 配饰 (category_id=3) — 已有: 手工皮革托特包, 羊绒混纺围巾, 极简腕表40mm
-- ============================================================
INSERT INTO l_product (name, description, category_id, price, original_price, stock, badge, badge_text, image, sales, is_hot, is_new, sort_order) VALUES
('手工925银锁骨链', '925银镀18K金，淡水珍珠点缀。极细链身锁骨间闪耀，精致不张扬。', 3, 299.00, 399.00, 20, 'hot', '送礼推荐', 'https://images.unsplash.com/photo-1599643478518-a784e5dc4cfc?w=600', 345, 1, 0, 1),
('偏光太阳镜 飞行员款', '蔡司偏光镜片，钛合金框架仅重22g。UV400全波段防护。', 3, 699.00, NULL, 15, 'new', '夏日必备', 'https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=600', 67, 0, 1, 2),
('双层撞色雨伞', '16骨加固防风骨架，双层伞布撞色设计。晴雨两用UPF50+。', 3, 199.00, 299.00, 30, 'sale', '限时折扣', 'https://images.unsplash.com/photo-1523354351435-fa5e87ab4802?w=600', 234, 0, 0, 3),
('头层牛皮卡包', '意大利植鞣牛皮，6卡位设计。手掌大小可放口袋。养牛玩家必备。', 3, 259.00, NULL, 25, NULL, '', 'https://images.unsplash.com/photo-1606503825005-3351c2f1275e?w=600', 178, 0, 0, 4),
('旅行收纳七件套', '防水尼龙面料，七件套分类收纳。包含衣物袋/鞋袋/洗漱包/内衣袋。', 3, 169.00, 249.00, 40, 'hot', '出行必备', 'https://images.unsplash.com/photo-1553530979-7ee52a2670c4?w=600', 523, 1, 0, 5);

-- ============================================================
-- 生活方式 (category_id=4) — 已有: 手工陶瓷香氛杯
-- ============================================================
INSERT INTO l_product (name, description, category_id, price, original_price, stock, badge, badge_text, image, sales, is_hot, is_new, sort_order) VALUES
('天然大豆蜡香薰蜡烛', '100%天然大豆蜡，法国进口精油。燃烧时间50小时。推荐香型：白茶/檀木/海盐。', 4, 189.00, NULL, 30, 'hot', '人气单品', 'https://images.unsplash.com/photo-1602874801006-22f27a5a4625?w=600', 567, 1, 0, 1),
('日式手工粗陶茶具套装', '景德镇手工拉胚，草木灰釉自然窑变。一壶两杯一公道，静心品茗。', 4, 499.00, 699.00, 10, 'new', '匠人作品', 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=600', 134, 0, 1, 2),
('北欧极简台灯', '黄铜灯杆+胡桃木底座，三档调光。暖光3000K护眼不刺眼，书桌床头两用。', 4, 459.00, NULL, 15, NULL, '', 'https://images.unsplash.com/photo-1507473885765-e6ed057ab6fe?w=600', 89, 0, 0, 3),
('100支长绒棉四件套', '新疆长绒棉100支贡缎，丝般顺滑。活性印染不褪色不起球。1.8m床适用。', 4, 699.00, 999.00, 12, 'sale', '床品特惠', 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=600', 201, 0, 0, 4),
('天然藤编收纳篮套装', '越南进口天然水葫芦藤，手工编织。大中小三件套，客厅卧室都能用。', 4, 229.00, NULL, 20, 'new', '家居好物', 'https://images.unsplash.com/photo-1602028915047-37269d1a73f7?w=600', 56, 0, 1, 5);

-- ============================================================
-- 秒杀活动示例（为几个热销品设置秒杀）
-- ============================================================
UPDATE l_product SET seckill_price = 299.00, seckill_stock = 20, seckill_start = NOW(), seckill_end = DATE_ADD(NOW(), INTERVAL 24 HOUR) WHERE id IN (
    (SELECT id FROM (SELECT id FROM l_product WHERE name = '法式复古碎花连衣裙' LIMIT 1) t)
);
UPDATE l_product SET seckill_price = 399.00, seckill_stock = 15, seckill_start = NOW(), seckill_end = DATE_ADD(NOW(), INTERVAL 24 HOUR) WHERE id IN (
    (SELECT id FROM (SELECT id FROM l_product WHERE name = '有机棉休闲T恤' LIMIT 1) t)
);
UPDATE l_product SET seckill_price = 9.90, seckill_stock = 30, seckill_start = NOW(), seckill_end = DATE_ADD(NOW(), INTERVAL 24 HOUR) WHERE id IN (
    (SELECT id FROM (SELECT id FROM l_product WHERE name = '天然大豆蜡香薰蜡烛' LIMIT 1) t)
);

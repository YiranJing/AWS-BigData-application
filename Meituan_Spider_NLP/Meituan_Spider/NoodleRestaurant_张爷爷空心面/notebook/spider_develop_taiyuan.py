#!/usr/bin/env python3
# -*- coding: utf-8 -*-

'''
Define a MeituanSpider class allows you to fetch meituan
restaurants infos in taiyuan city.
'''

import csv
import json
import os
import requests
import pymysql
import random
import time
from pymongo import MongoClient
from py2neo import Graph, Node
from settings import headers,limit

# Data storage path and filename(.csv or .txt file) setting variables:
savePath = './meituanZhangYeyeInfos'
filename = 'ShangHaiRestaurants'


class MeituanSpider_taiyuan(object):
    '''
    MeituanSpider class allows you to fetch all data from meituan api website.
    :Usage:
    '''
    ## 太原茂业中心店
    baseUrl_1 = ("https://www.meituan.com/meishi/api/poi/getMerchantComment?uuid=57461b13249c4026be0d.1564202509.1.0.0&platform=1&partner=126&originUrl=https%3A%2F%2Fwww.meituan.com%2Fmeishi%2F193639293%2F&riskLevel=1&optimusCode=10&id=193639293&userId=&offset=")
   
  
    def __init__(self, saveMode='csv'):
        print("Spider is ready for Taiyuan area")

    def run(self,number_comments):
        url = self.baseUrl_1 + str(number_comments)+ '&offset=%s&pageSize=10&sortType=1' # number_comments should be large enough
        taglist_1, itemlist_1 = self.parse(url, '太原茂业中心店')
                
        taglist = taglist_1
        itemlist = itemlist_1 
        return taglist, itemlist     
        
    def parse(self,url, store_name):
        response = requests.get(url,headers=random.choice(headers))
        number = 0
        while True:
            try:
                info_dict = json.loads(response.text)
                info_list = info_dict['data'] 
                if info_list:
                    break
                else:
                    print('some wrong')
                    number += 1
                    if number >= 10:
                        return None
                    time.sleep(10)
                    response = requests.get(url, headers=random.choice(headers))
            except:
                number += 1
                if number >= 10:
                    return None
                time.sleep(10)
                response = requests.get(url, headers=random.choice(headers))
        
        ##用户总体评价   
        taglist = [] 
        try:
            for comment in info_list['tags']:
                count = comment['count']
                tag = comment['tag']
            
                tag_item = {
                    '标签':tag,
                    '累计数量':count
                }
                taglist.append(tag_item)
        except TypeError: # if no tags information
            taglist = [] 
        
        ##用户具体评价
        itemlist = []
        for comment in info_list['comments']:
            username = comment['userName']
            avgPrice = comment['avgPrice']
            user_comment = comment['comment']  
            commenttime = comment['commentTime']
            menu = comment['menu']
            dealEndtime = comment['dealEndtime']
            star = comment['star']
            
            item = {
                    '店名': store_name,
                    '用户名': username,
                    '平均消费': avgPrice,
                    '星级': star,
                    '菜单': menu,
                    '评价时间': commenttime,
                    '用餐结束时间': dealEndtime,
                    '评价': user_comment
                }
            itemlist.append(item)       
        return taglist, itemlist


    def __del__(self):
        '''
        The deconstructor of MeituanSpider class.
        Deconstructs an instance of MeituanSpider, closes MongoDB database and
        files.
        '''
        print('>>>> Finish.')
            

# test:
if __name__ == '__main__':
    spider = MeituanSpider(saveMode='csv')
    spider.run()
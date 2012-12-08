#!/usr/bin/ruby -w

fname = ARGV[0]
rgbs = File.open("#{fname}").readlines.grep(/<integer>/).map{|line| Float(line.scan(/\d+/)[0])/255 }

unless 768 == rgbs.length
  raise "#{fname}: not exactly 768 integers found in there"
end
# puts rgbs.inspect 

blues = rgbs[0...256]
greens = rgbs[256...512]
reds = rgbs[512...768]

require 'rubygems'
require 'gd2'
include GD2

image = Image::TrueColor.new(256, 1)
image.draw do |pen|
  (0...256).each do |x|
    pen.color = image.palette.resolve Color[reds[x], greens[x], blues[x]]
    pen.move_to x, 0
    pen.line_to(x,0)
  end
end

out_fname = fname.gsub(/plist/,"png")
out_fname += ".png" if out_fname == fname

image.export(out_fname)

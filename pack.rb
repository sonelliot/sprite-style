# !/bin/ruby

require 'fileutils'
include FileUtils

def pixelate (from, to)
  %x[ imconvert -filter Box -define filter:blur=0.1875 -resize 64x64 '#{from}' '#{to}' ]
end

def pixelate_all (name, files)
  files.each do |f|
    m = /(\d+)_#{name}.+?(\d+).png/.match(f)
    if m
      angle = m.captures[0]
      frame = m.captures[1].to_i
      dir = "#{name}-#{angle}"
      if not Dir.exists? dir
        mkdir dir
      end
      pixelate(f, "#{dir}/#{name}-#{angle}-#{frame}.png")
    end
  end
end

def pack (name)
  cmd = "texturepacker --data #{name}.json --sheet #{name}.png --format json " +
        "--algorithm Basic --basic-order Ascending --trim-mode None " +
        "--force-squared #{name}"
   %x[ #{cmd} ]
end

def pack_all (name, dirs)
  dirs.each do |d|
    if /#{name}-\d+/ =~ d
      pack d
    end
  end
end

anim = ARGV[0]
pixelate_all(anim, Dir["*#{anim}*.png"])
pack_all(anim, Dir['./*/'].map{|d| File.basename d})
